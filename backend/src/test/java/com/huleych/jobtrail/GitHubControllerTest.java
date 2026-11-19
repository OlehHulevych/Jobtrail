package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.application.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GithubRegisterRequest;
import com.huleych.jobtrail.identity.application.service.GithubAuthService;
import com.huleych.jobtrail.identity.domain.Skill;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import com.huleych.jobtrail.identity.domain.exceptions.GithubAuthenticationException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundByEmailException;
import com.huleych.jobtrail.identity.web.GitHubController;
import com.huleych.jobtrail.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GitHubControllerTest {
    private static final String CODE = "github-code";
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    GithubAuthService githubAuthService;
    MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GitHubController(githubAuthService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void loginShouldReturnTokens() throws Exception {
        when(githubAuthService.login(CODE)).thenReturn(new LoginResult("access", "refresh"));

        mockMvc.perform(post("/api/auth/github/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + CODE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    public void loginShouldReturnBadRequestWhenCodeIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/github/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(githubAuthService);
    }

    @Test
    public void loginShouldReturnUnauthorizedWhenGithubAuthenticationFails() throws Exception {
        when(githubAuthService.login(CODE)).thenThrow(new GithubAuthenticationException("bad code"));

        mockMvc.perform(post("/api/auth/github/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + CODE + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(githubAuthService.login(CODE)).thenThrow(new UserNotFoundByEmailException("user@example.com"));

        mockMvc.perform(post("/api/auth/github/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + CODE + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void registerShouldReturnIdAndEmailAndPassSkillsToService() throws Exception {
        User user = new User("user@example.com", null, Set.of());
        ReflectionTestUtils.setField(user, "id", USER_ID);
        when(githubAuthService.register(any(GithubRegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/github/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + CODE + "\",\"skills\":[{\"name\":\"java\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        ArgumentCaptor<GithubRegisterRequest> captor = ArgumentCaptor.forClass(GithubRegisterRequest.class);
        verify(githubAuthService).register(captor.capture());
        assertEquals(CODE, captor.getValue().code());
        assertEquals(Set.of(new Skill("java")), captor.getValue().skills());
    }

    @Test
    public void registerShouldReturnBadRequestWhenCodeIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/github/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(githubAuthService);
    }

    @Test
    public void registerShouldReturnConflictWhenEmailIsAlreadyTaken() throws Exception {
        when(githubAuthService.register(any(GithubRegisterRequest.class)))
                .thenThrow(new EmailAlreadyTakenException("user@example.com"));

        mockMvc.perform(post("/api/auth/github/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + CODE + "\"}"))
                .andExpect(status().isConflict());
    }
}
