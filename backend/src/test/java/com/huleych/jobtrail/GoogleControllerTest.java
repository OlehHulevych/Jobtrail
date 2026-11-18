package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.application.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GoogleRegisterRequest;
import com.huleych.jobtrail.identity.application.service.GoogleAuthService;
import com.huleych.jobtrail.identity.domain.Skill;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundByEmailException;
import com.huleych.jobtrail.identity.web.GoogleController;
import com.huleych.jobtrail.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GoogleControllerTest {
    private static final String ID_TOKEN = "google-id-token";
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    GoogleAuthService googleAuthService;
    MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GoogleController(googleAuthService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void loginShouldReturnTokens() throws Exception {
        when(googleAuthService.login(ID_TOKEN)).thenReturn(new LoginResult("access", "refresh"));

        mockMvc.perform(post("/api/auth/google/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"" + ID_TOKEN + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    public void loginShouldReturnBadRequestWhenIdTokenIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/google/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(googleAuthService);
    }

    @Test
    public void loginShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(googleAuthService.login(ID_TOKEN)).thenThrow(new UserNotFoundByEmailException("user@example.com"));

        mockMvc.perform(post("/api/auth/google/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"" + ID_TOKEN + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void loginShouldReturnUnauthorizedWhenGoogleTokenIsInvalid() throws Exception {
        when(googleAuthService.login(ID_TOKEN))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid google token"));

        mockMvc.perform(post("/api/auth/google/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"" + ID_TOKEN + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void registerShouldReturnIdAndEmailAndPassSkillsToService() throws Exception {
        User user = new User("user@example.com", null, Set.of());
        ReflectionTestUtils.setField(user, "id", USER_ID);
        when(googleAuthService.register(any(GoogleRegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/google/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"" + ID_TOKEN + "\",\"skills\":[{\"name\":\"java\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        ArgumentCaptor<GoogleRegisterRequest> captor = ArgumentCaptor.forClass(GoogleRegisterRequest.class);
        verify(googleAuthService).register(captor.capture());
        assertEquals(ID_TOKEN, captor.getValue().idToken());
        assertEquals(Set.of(new Skill("java")), captor.getValue().skills());
    }

    @Test
    public void registerShouldReturnBadRequestWhenIdTokenIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/google/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(googleAuthService);
    }

    @Test
    public void registerShouldReturnConflictWhenEmailIsAlreadyTaken() throws Exception {
        when(googleAuthService.register(any(GoogleRegisterRequest.class)))
                .thenThrow(new EmailAlreadyTakenException("user@example.com"));

        mockMvc.perform(post("/api/auth/google/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"" + ID_TOKEN + "\"}"))
                .andExpect(status().isConflict());
    }
}
