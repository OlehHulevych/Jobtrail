package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.domain.exceptions.CvDocumentNotFoundException;
import com.huleych.jobtrail.identity.domain.exceptions.NotCvDocumentOwnerException;
import com.huleych.jobtrail.insights.application.commands.AnalyzeMatchCommand;
import com.huleych.jobtrail.insights.application.result.MatchAnalysisResult;
import com.huleych.jobtrail.insights.application.service.CvAnalysisChatService;
import com.huleych.jobtrail.insights.domain.exceptions.CvAnalysisFailedException;
import com.huleych.jobtrail.insights.web.controller.CvAnalysisController;
import com.huleych.jobtrail.shared.web.GlobalExceptionHandler;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CvAnalysisControllerTest {
    private static final String URL = "/api/cv/analysis";
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID CV_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final String BODY = "{\"applicationId\":\"" + APPLICATION_ID + "\",\"cvId\":\"" + CV_ID + "\"}";
    private static final AnalyzeMatchCommand COMMAND = new AnalyzeMatchCommand(USER_ID, APPLICATION_ID, CV_ID);

    @Mock
    CvAnalysisChatService cvAnalysisChatService;
    MockMvc mockMvc;

    @BeforeEach
    public void init() {
        User user = new User("Test User", "user@example.com", "hash", Set.of());
        ReflectionTestUtils.setField(user, "id", USER_ID);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        mockMvc = MockMvcBuilders.standaloneSetup(new CvAnalysisController(cvAnalysisChatService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequestBuilder analyzeRequest(String body) {
        return post(URL).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    public void shouldReturnCreatedWithAnalysisResult() throws Exception {
        MatchAnalysisResult result = new MatchAnalysisResult(80, List.of("Java"), List.of("Docker"), List.of("Add Docker projects"));
        when(cvAnalysisChatService.execute(COMMAND)).thenReturn(result);

        mockMvc.perform(analyzeRequest(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matchScore").value(80))
                .andExpect(jsonPath("$.matchedSkills[0]").value("Java"))
                .andExpect(jsonPath("$.missingSkills[0]").value("Docker"))
                .andExpect(jsonPath("$.suggestions[0]").value("Add Docker projects"));
    }

    @Test
    public void shouldUseCurrentUserIdFromPrincipalInCommand() throws Exception {
        when(cvAnalysisChatService.execute(any(AnalyzeMatchCommand.class)))
                .thenReturn(new MatchAnalysisResult(50, List.of(), List.of(), List.of()));

        mockMvc.perform(analyzeRequest(BODY)).andExpect(status().isCreated());

        verify(cvAnalysisChatService).execute(new AnalyzeMatchCommand(USER_ID, APPLICATION_ID, CV_ID));
    }

    @Test
    public void shouldIgnoreUserIdSentInBody() throws Exception {
        UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        String body = "{\"userId\":\"" + otherUserId + "\",\"applicationId\":\"" + APPLICATION_ID + "\",\"cvId\":\"" + CV_ID + "\"}";
        when(cvAnalysisChatService.execute(any(AnalyzeMatchCommand.class)))
                .thenReturn(new MatchAnalysisResult(50, List.of(), List.of(), List.of()));

        mockMvc.perform(analyzeRequest(body)).andExpect(status().isCreated());

        verify(cvAnalysisChatService).execute(new AnalyzeMatchCommand(USER_ID, APPLICATION_ID, CV_ID));
    }

    @Test
    public void shouldReturnNotFoundWhenApplicationDoesNotExist() throws Exception {
        when(cvAnalysisChatService.execute(COMMAND)).thenThrow(new ApplicationNotFoundException(APPLICATION_ID));

        mockMvc.perform(analyzeRequest(BODY)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenCvDocumentDoesNotExist() throws Exception {
        when(cvAnalysisChatService.execute(COMMAND)).thenThrow(new CvDocumentNotFoundException(CV_ID));

        mockMvc.perform(analyzeRequest(BODY)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnForbiddenWhenCallerDoesNotOwnApplication() throws Exception {
        when(cvAnalysisChatService.execute(COMMAND)).thenThrow(new NotApplicationOwnerException(USER_ID, APPLICATION_ID));

        mockMvc.perform(analyzeRequest(BODY)).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenCallerDoesNotOwnCvDocument() throws Exception {
        when(cvAnalysisChatService.execute(COMMAND)).thenThrow(new NotCvDocumentOwnerException(USER_ID, CV_ID));

        mockMvc.perform(analyzeRequest(BODY)).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnConflictWhenTextIsNotExtracted() throws Exception {
        when(cvAnalysisChatService.execute(COMMAND)).thenThrow(new IllegalStateException("Text is not extracted"));

        mockMvc.perform(analyzeRequest(BODY)).andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnBadGatewayWhenAiReturnsNoAnalysis() throws Exception {
        when(cvAnalysisChatService.execute(COMMAND)).thenThrow(new CvAnalysisFailedException(CV_ID));

        mockMvc.perform(analyzeRequest(BODY)).andExpect(status().isBadGateway());
    }

    @Test
    public void shouldReturnBadRequestWhenServiceRejectsMissingIds() throws Exception {
        when(cvAnalysisChatService.execute(new AnalyzeMatchCommand(USER_ID, null, null)))
                .thenThrow(new IllegalArgumentException("job application id is required"));

        mockMvc.perform(analyzeRequest("{}")).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenBodyIsNotValidJson() throws Exception {
        mockMvc.perform(analyzeRequest("not json")).andExpect(status().isBadRequest());

        verifyNoInteractions(cvAnalysisChatService);
    }

    @Test
    public void shouldReturnBadRequestWhenIdIsNotAUuid() throws Exception {
        mockMvc.perform(analyzeRequest("{\"applicationId\":\"not-a-uuid\",\"cvId\":\"" + CV_ID + "\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cvAnalysisChatService);
    }
}
