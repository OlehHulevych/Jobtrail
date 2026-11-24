package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.insights.application.JobAnalysisChatService;
import com.huleych.jobtrail.insights.domain.Seniority;
import com.huleych.jobtrail.insights.domain.exceptions.JobAnalysisFailedException;
import com.huleych.jobtrail.insights.web.JobAnalysisController;
import com.huleych.jobtrail.insights.web.JobAnalysisResponse;
import com.huleych.jobtrail.shared.web.GlobalExceptionHandler;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class JobAnalysisControllerTest {
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ANALYSIS_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @Mock
    JobAnalysisChatService jobAnalysisChatService;
    MockMvc mockMvc;

    @BeforeEach
    public void init() {
        User user = new User("Test User", "user@example.com", "hash", Set.of());
        ReflectionTestUtils.setField(user, "id", USER_ID);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        mockMvc = MockMvcBuilders.standaloneSetup(new JobAnalysisController(jobAnalysisChatService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldReturnAnalysisForCurrentUser() throws Exception {
        JobAnalysisResponse response = new JobAnalysisResponse(ANALYSIS_ID, APPLICATION_ID, "A backend role",
                Seniority.MID, List.of("Java"), List.of("Docker"), List.of("Build APIs"), List.of("Constant availability"),
                LocalDateTime.of(2026, 11, 20, 10, 0));
        when(jobAnalysisChatService.execute(USER_ID, APPLICATION_ID)).thenReturn(response);

        mockMvc.perform(post("/api/applications/{id}/analysis", APPLICATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ANALYSIS_ID.toString()))
                .andExpect(jsonPath("$.applicationId").value(APPLICATION_ID.toString()))
                .andExpect(jsonPath("$.summary").value("A backend role"))
                .andExpect(jsonPath("$.seniority").value("MID"))
                .andExpect(jsonPath("$.requiredSkills[0]").value("Java"))
                .andExpect(jsonPath("$.niceToHaveSkills[0]").value("Docker"))
                .andExpect(jsonPath("$.responsibilities[0]").value("Build APIs"))
                .andExpect(jsonPath("$.redFlags[0]").value("Constant availability"));

        verify(jobAnalysisChatService).execute(USER_ID, APPLICATION_ID);
    }

    @Test
    public void shouldReturnNotFoundWhenApplicationDoesNotExist() throws Exception {
        when(jobAnalysisChatService.execute(USER_ID, APPLICATION_ID)).thenThrow(new ApplicationNotFoundException(APPLICATION_ID));

        mockMvc.perform(post("/api/applications/{id}/analysis", APPLICATION_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnForbiddenWhenCallerIsNotOwner() throws Exception {
        when(jobAnalysisChatService.execute(USER_ID, APPLICATION_ID))
                .thenThrow(new NotApplicationOwnerException(USER_ID, APPLICATION_ID));

        mockMvc.perform(post("/api/applications/{id}/analysis", APPLICATION_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnBadGatewayWhenAiReturnsNoAnalysis() throws Exception {
        when(jobAnalysisChatService.execute(USER_ID, APPLICATION_ID)).thenThrow(new JobAnalysisFailedException(APPLICATION_ID));

        mockMvc.perform(post("/api/applications/{id}/analysis", APPLICATION_ID))
                .andExpect(status().isBadGateway());
    }

    @Test
    public void shouldReturnBadRequestWhenIdIsNotAUuid() throws Exception {
        mockMvc.perform(post("/api/applications/{id}/analysis", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jobAnalysisChatService);
    }
}
