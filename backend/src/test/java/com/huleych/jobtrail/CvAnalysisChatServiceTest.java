package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.domain.entity.CvDocument;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.domain.exceptions.CvDocumentNotFoundException;
import com.huleych.jobtrail.identity.domain.exceptions.NotCvDocumentOwnerException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundException;
import com.huleych.jobtrail.identity.infrastructure.repository.CvDocumentRepository;
import com.huleych.jobtrail.identity.infrastructure.repository.UserRepository;
import com.huleych.jobtrail.insights.application.commands.AnalyzeMatchCommand;
import com.huleych.jobtrail.insights.application.result.MatchAnalysisResult;
import com.huleych.jobtrail.insights.application.service.CvAnalysisChatService;
import com.huleych.jobtrail.insights.domain.entity.CvMatchAnalysis;
import com.huleych.jobtrail.insights.domain.enums.AnalysisStatus;
import com.huleych.jobtrail.insights.domain.exceptions.CvAnalysisFailedException;
import com.huleych.jobtrail.insights.infrastructure.repository.CvMatchAnalysisRepository;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.infrastructure.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CvAnalysisChatServiceTest {
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID CV_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final String JOB_DESCRIPTION = "Build backend services";
    private static final String CV_TEXT = "Java developer with Spring Boot experience";
    private static final AnalyzeMatchCommand COMMAND = new AnalyzeMatchCommand(USER_ID, APPLICATION_ID, CV_ID);

    @Mock
    UserRepository userRepository;
    @Mock
    JobApplicationRepository jobApplicationRepository;
    @Mock
    CvDocumentRepository cvDocumentRepository;
    @Mock
    CvMatchAnalysisRepository cvMatchAnalysisRepository;
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
    ArgumentCaptor<CvMatchAnalysis> analysisCaptor;

    Resource analysisPrompt = new ByteArrayResource("system prompt".getBytes());
    Resource userPrompt = new ByteArrayResource("user prompt {jobDescription} {cvText}".getBytes());
    User user;
    JobApplication application;
    CvDocument cvDocument;
    CvAnalysisChatService service;

    @BeforeEach
    public void init() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        service = new CvAnalysisChatService(userRepository, jobApplicationRepository, cvDocumentRepository,
                analysisPrompt, chatClientBuilder, userPrompt, cvMatchAnalysisRepository);
        user = userWithId(USER_ID);
        application = applicationOwnedBy(USER_ID);
        cvDocument = extractedCvOwnedBy(user);
    }

    private User userWithId(UUID id) {
        User created = new User("Test User", "user" + id + "@example.com", "hash", Set.of());
        ReflectionTestUtils.setField(created, "id", id);
        return created;
    }

    private JobApplication applicationOwnedBy(UUID ownerId) {
        JobApplication created = new JobApplication(ownerId, "Acme", "Java Developer", "https://acme.example/jobs/1", JOB_DESCRIPTION);
        ReflectionTestUtils.setField(created, "id", APPLICATION_ID);
        return created;
    }

    private CvDocument extractedCvOwnedBy(User owner) {
        CvDocument created = new CvDocument(owner, "cv/" + owner.getId() + "/key", "cv.pdf", "application/pdf");
        ReflectionTestUtils.setField(created, "id", CV_ID);
        created.markExtracted(CV_TEXT);
        return created;
    }

    private void stubLookups() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(cvDocumentRepository.findById(CV_ID)).thenReturn(Optional.of(cvDocument));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private void stubAi(MatchAnalysisResult result) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.options(any(GoogleGenAiChatOptions.Builder.class))).thenReturn(requestSpec);
        when(requestSpec.system(analysisPrompt)).thenReturn(requestSpec);
        when(requestSpec.user(ArgumentMatchers.<Consumer<ChatClient.PromptUserSpec>>any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.entity(MatchAnalysisResult.class)).thenReturn(result);
    }

    private MatchAnalysisResult aiResult(int score) {
        return new MatchAnalysisResult(score, List.of("Java", "Spring Boot"), List.of("Docker"),
                List.of("Mention Docker", "Add metrics"));
    }

    @Test
    public void shouldAnalyzeAndSaveMatchAnalysis() {
        stubLookups();
        MatchAnalysisResult result = aiResult(80);
        stubAi(result);

        MatchAnalysisResult returned = service.execute(COMMAND);

        assertSame(result, returned);
        verify(cvMatchAnalysisRepository).save(analysisCaptor.capture());
        CvMatchAnalysis saved = analysisCaptor.getValue();
        assertSame(application, saved.getJobApplication());
        assertSame(cvDocument, saved.getCvDocument());
        assertSame(user, saved.getUser());
        assertEquals(80, saved.getMatchScore());
        assertEquals(List.of("Java", "Spring Boot"), saved.getMatchedSkills());
        assertEquals(List.of("Docker"), saved.getMissingSkills());
        assertEquals("Mention Docker, Add metrics", saved.getSuggestions());
        assertEquals(AnalysisStatus.PENDING, saved.getStatus());
    }

    @Test
    public void shouldSendPromptsJobDescriptionAndCvTextToAi() {
        stubLookups();
        stubAi(aiResult(80));

        service.execute(COMMAND);

        verify(requestSpec).system(analysisPrompt);
        verify(requestSpec).user(userCaptor.capture());
        ChatClient.PromptUserSpec userSpec = mock(ChatClient.PromptUserSpec.class);
        when(userSpec.text(userPrompt)).thenReturn(userSpec);
        when(userSpec.param("jobDescription", JOB_DESCRIPTION)).thenReturn(userSpec);
        userCaptor.getValue().accept(userSpec);
        verify(userSpec).param("jobDescription", JOB_DESCRIPTION);
        verify(userSpec).param("cvText", CV_TEXT);
    }

    @Test
    public void shouldSaveNullSuggestionsWhenAiReturnsNone() {
        stubLookups();
        stubAi(new MatchAnalysisResult(70, List.of("Java"), List.of(), null));

        service.execute(COMMAND);

        verify(cvMatchAnalysisRepository).save(analysisCaptor.capture());
        assertNull(analysisCaptor.getValue().getSuggestions());
    }

    @Test
    public void shouldSaveEmptySkillListsWhenAiReturnsNullSkills() {
        stubLookups();
        MatchAnalysisResult result = new MatchAnalysisResult(70, null, null, List.of("Add metrics"));
        stubAi(result);

        MatchAnalysisResult returned = service.execute(COMMAND);

        assertSame(result, returned);
        verify(cvMatchAnalysisRepository).save(analysisCaptor.capture());
        assertEquals(List.of(), analysisCaptor.getValue().getMatchedSkills());
        assertEquals(List.of(), analysisCaptor.getValue().getMissingSkills());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100})
    public void shouldAcceptBoundaryScores(int score) {
        stubLookups();
        stubAi(aiResult(score));

        MatchAnalysisResult returned = service.execute(COMMAND);

        assertEquals(score, returned.matchScore());
        verify(cvMatchAnalysisRepository).save(any(CvMatchAnalysis.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    public void shouldThrowAnalysisFailedAndNotSaveWhenScoreIsOutOfRange(int score) {
        stubLookups();
        stubAi(aiResult(score));

        assertThrows(CvAnalysisFailedException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(cvMatchAnalysisRepository);
    }

    @Test
    public void shouldThrowAnalysisFailedAndNotSaveWhenAiReturnsNull() {
        stubLookups();
        stubAi(null);

        assertThrows(CvAnalysisFailedException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(cvMatchAnalysisRepository);
    }

    @Test
    public void shouldThrowApplicationNotFoundAndStopThere() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(cvDocumentRepository, userRepository, chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldThrowCvDocumentNotFoundAndStopThere() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(cvDocumentRepository.findById(CV_ID)).thenReturn(Optional.empty());

        assertThrows(CvDocumentNotFoundException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(userRepository, chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldThrowUserNotFoundAndStopThere() {
        when(jobApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(cvDocumentRepository.findById(CV_ID)).thenReturn(Optional.of(cvDocument));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldThrowNotCvOwnerWhenCvBelongsToAnotherUser() {
        cvDocument = extractedCvOwnedBy(userWithId(OTHER_USER_ID));
        stubLookups();

        assertThrows(NotCvDocumentOwnerException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldThrowNotApplicationOwnerWhenApplicationBelongsToAnotherUser() {
        application = applicationOwnedBy(OTHER_USER_ID);
        stubLookups();

        assertThrows(NotApplicationOwnerException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldThrowIllegalStateWhenTextIsStillPending() {
        cvDocument = new CvDocument(user, "cv/key", "cv.pdf", "application/pdf");
        ReflectionTestUtils.setField(cvDocument, "id", CV_ID);
        stubLookups();

        assertThrows(IllegalStateException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldThrowIllegalStateWhenExtractionFailed() {
        cvDocument = new CvDocument(user, "cv/key", "cv.pdf", "application/pdf");
        ReflectionTestUtils.setField(cvDocument, "id", CV_ID);
        cvDocument.markExtractionFailed();
        stubLookups();

        assertThrows(IllegalStateException.class, () -> service.execute(COMMAND));

        verifyNoInteractions(chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldRejectNullCommand() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null));

        verifyNoInteractions(userRepository, jobApplicationRepository, cvDocumentRepository, chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldRejectNullApplicationId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(new AnalyzeMatchCommand(USER_ID, null, CV_ID)));

        verifyNoInteractions(userRepository, jobApplicationRepository, cvDocumentRepository, chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldRejectNullCvId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(new AnalyzeMatchCommand(USER_ID, APPLICATION_ID, null)));

        verifyNoInteractions(userRepository, jobApplicationRepository, cvDocumentRepository, chatClient, cvMatchAnalysisRepository);
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(new AnalyzeMatchCommand(null, APPLICATION_ID, CV_ID)));

        verifyNoInteractions(userRepository, jobApplicationRepository, cvDocumentRepository, chatClient, cvMatchAnalysisRepository);
    }
}
