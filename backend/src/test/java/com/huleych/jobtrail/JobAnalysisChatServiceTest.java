package com.huleych.jobtrail;

import com.huleych.jobtrail.insights.application.service.JobAnalysisChatService;
import com.huleych.jobtrail.insights.application.result.JobAnalysisResult;
import com.huleych.jobtrail.insights.domain.entity.JobAnalysis;
import com.huleych.jobtrail.insights.infrastructure.repository.JobAnalysisRepository;
import com.huleych.jobtrail.insights.domain.enums.Seniority;
import com.huleych.jobtrail.insights.domain.exceptions.JobAnalysisFailedException;
import com.huleych.jobtrail.insights.web.response.JobAnalysisResponse;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import com.huleych.jobtrail.tracking.infrastructure.repository.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobAnalysisChatServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final String JOB_DESCRIPTION = "Build backend services";

    @Mock
    JobApplicationRepository jobApplicationRepository;
    @Mock
    JobAnalysisRepository jobAnalysisRepository;
    @Mock
    ChatClient.Builder chatClientBuilder;
    @Mock
    ChatClient chatClient;
    @Mock
    ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    ChatClient.CallResponseSpec callSpec;
    @Captor
    ArgumentCaptor<Consumer<ChatClient.PromptUserSpec>> userCaptor;
    @Captor
    ArgumentCaptor<JobAnalysis> analysisCaptor;

    Resource promptResource = new ByteArrayResource("prompt {jobDescription}".getBytes());
    JobApplication application;
    JobAnalysisChatService service;

    @BeforeEach
    public void init() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        service = new JobAnalysisChatService(jobApplicationRepository, promptResource, chatClientBuilder, jobAnalysisRepository);
        application = new JobApplication(OWNER_ID, "Acme", "Java Developer", "https://acme.example/jobs/1", JOB_DESCRIPTION);
        ReflectionTestUtils.setField(application, "id", APPLICATION_ID);
    }

    private void stubAi(JobAnalysisResult result) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.options(any(GoogleGenAiChatOptions.Builder.class))).thenReturn(requestSpec);
        when(requestSpec.user(ArgumentMatchers.<Consumer<ChatClient.PromptUserSpec>>any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.entity(JobAnalysisResult.class)).thenReturn(result);
    }

    private JobAnalysisResult aiResult() {
        return new JobAnalysisResult("A backend role", Seniority.MID, List.of("Java", "Spring Boot"),
                List.of("Docker"), List.of("Build APIs"), List.of("Constant availability expected"));
    }

    @Test
    public void shouldCreateAndSaveAnalysisWhenNoneExists() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        stubAi(aiResult());
        when(jobAnalysisRepository.findByApplicationId(APPLICATION_ID)).thenReturn(Optional.empty());
        when(jobAnalysisRepository.save(any(JobAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));

        JobAnalysisResponse response = service.execute(OWNER_ID, APPLICATION_ID);

        verify(jobAnalysisRepository).save(analysisCaptor.capture());
        assertEquals(APPLICATION_ID, analysisCaptor.getValue().getApplicationId());
        assertEquals(APPLICATION_ID, response.applicationId());
        assertEquals("A backend role", response.summary());
        assertEquals(Seniority.MID, response.seniority());
        assertEquals(List.of("Java", "Spring Boot"), response.requiredSkills());
        assertEquals(List.of("Docker"), response.niceToHaveSkills());
        assertEquals(List.of("Build APIs"), response.responsibilities());
        assertEquals(List.of("Constant availability expected"), response.redFlags());
    }

    @Test
    public void shouldReplaceExistingAnalysisInsteadOfCreatingNewOne() {
        JobAnalysis existing = new JobAnalysis(APPLICATION_ID, "old summary", Seniority.JUNIOR,
                List.of("PHP"), List.of(), List.of(), List.of());
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        stubAi(aiResult());
        when(jobAnalysisRepository.findByApplicationId(APPLICATION_ID)).thenReturn(Optional.of(existing));
        when(jobAnalysisRepository.save(any(JobAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));

        JobAnalysisResponse response = service.execute(OWNER_ID, APPLICATION_ID);

        verify(jobAnalysisRepository).save(analysisCaptor.capture());
        assertSame(existing, analysisCaptor.getValue());
        assertEquals("A backend role", existing.getSummary());
        assertEquals(Seniority.MID, existing.getSeniority());
        assertEquals(List.of("Java", "Spring Boot"), existing.getRequiredSkills());
        assertEquals("A backend role", response.summary());
    }

    @Test
    public void shouldSendPromptResourceAndJobDescriptionToAi() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        stubAi(aiResult());
        when(jobAnalysisRepository.findByApplicationId(APPLICATION_ID)).thenReturn(Optional.empty());
        when(jobAnalysisRepository.save(any(JobAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));

        service.execute(OWNER_ID, APPLICATION_ID);

        verify(requestSpec).user(userCaptor.capture());
        ChatClient.PromptUserSpec userSpec = mock(ChatClient.PromptUserSpec.class);
        when(userSpec.text(promptResource)).thenReturn(userSpec);
        userCaptor.getValue().accept(userSpec);
        verify(userSpec).param("jobDescription", JOB_DESCRIPTION);
    }

    @Test
    public void shouldThrowJobAnalysisFailedWhenAiReturnsNull() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        stubAi(null);

        assertThrows(JobAnalysisFailedException.class, () -> service.execute(OWNER_ID, APPLICATION_ID));
        verifyNoInteractions(jobAnalysisRepository);
    }

    @Test
    public void shouldThrowApplicationNotFoundWhenApplicationDoesNotExist() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.execute(OWNER_ID, APPLICATION_ID));
        verifyNoInteractions(chatClient, jobAnalysisRepository);
    }

    @Test
    public void shouldThrowNotOwnerWhenCallerIsNotOwnerAndNotCallAi() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        assertThrows(NotApplicationOwnerException.class, () -> service.execute(OTHER_USER_ID, APPLICATION_ID));
        verifyNoInteractions(chatClient, jobAnalysisRepository);
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null, APPLICATION_ID));
        verifyNoInteractions(jobApplicationRepository, chatClient, jobAnalysisRepository);
    }

    @Test
    public void shouldRejectNullApplicationId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, null));
        verifyNoInteractions(jobApplicationRepository, chatClient, jobAnalysisRepository);
    }
}
