package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.application.commands.UploadCvCommand;
import com.huleych.jobtrail.identity.application.result.UploadResult;
import com.huleych.jobtrail.identity.application.service.CvUploadService;
import com.huleych.jobtrail.identity.domain.entity.CvDocument;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.domain.enums.ExtractionStatus;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundException;
import com.huleych.jobtrail.identity.infrastructure.repository.CvDocumentRepository;
import com.huleych.jobtrail.identity.infrastructure.repository.UserRepository;
import com.huleych.jobtrail.identity.infrastructure.storage.FileStorage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CvUploadServiceTest {
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID SAVED_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final String PDF = "application/pdf";
    private static final String ORIGINAL_NAME = "my-cv.pdf";
    private static final String CV_TEXT = "Java developer with Spring Boot experience";

    @Mock
    FileStorage storage;
    @Mock
    CvDocumentRepository cvDocumentRepository;
    @Mock
    UserRepository userRepository;
    @Captor
    ArgumentCaptor<String> keyCaptor;
    @Captor
    ArgumentCaptor<InputStream> streamCaptor;
    @Captor
    ArgumentCaptor<CvDocument> documentCaptor;

    User user;
    CvUploadService service;

    @BeforeEach
    public void init() {
        service = new CvUploadService(storage, cvDocumentRepository, userRepository);
        user = new User("Test User", "user@example.com", null, Set.of());
        ReflectionTestUtils.setField(user, "id", USER_ID);
    }

    private byte[] pdfWithText(String text) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            if (text != null) {
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(50, 700);
                    content.showText(text);
                    content.endText();
                }
            }
            document.save(out);
            return out.toByteArray();
        }
    }

    private UploadCvCommand command(byte[] bytes) {
        return new UploadCvCommand(USER_ID, ORIGINAL_NAME, PDF, bytes.length, new ByteArrayInputStream(bytes));
    }

    private void stubUserAndSave() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cvDocumentRepository.save(any(CvDocument.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    public void shouldExtractTextStoreFileAndSaveDocumentAsDone() throws IOException {
        byte[] bytes = pdfWithText(CV_TEXT);
        stubUserAndSave();

        UploadResult result = service.execute(command(bytes));

        verify(cvDocumentRepository).save(documentCaptor.capture());
        CvDocument saved = documentCaptor.getValue();
        assertEquals(ExtractionStatus.DONE, saved.getExtractionStatus());
        assertTrue(saved.getExtractedText().contains(CV_TEXT));
        assertSame(user, saved.getUser());
        assertEquals(ORIGINAL_NAME, saved.getOriginalName());
        assertEquals(PDF, saved.getContentType());
        assertEquals(ExtractionStatus.DONE, result.extractionStatus());
    }

    @Test
    public void shouldStoreOriginalBytesUnderUserScopedKey() throws IOException {
        byte[] bytes = pdfWithText(CV_TEXT);
        stubUserAndSave();

        service.execute(command(bytes));

        verify(storage).store(keyCaptor.capture(), streamCaptor.capture(), eq((long) bytes.length), eq(PDF));
        assertTrue(keyCaptor.getValue().startsWith("cv/" + USER_ID + "/"));
        assertArrayEquals(bytes, streamCaptor.getValue().readAllBytes());
        verify(cvDocumentRepository).save(documentCaptor.capture());
        assertEquals(keyCaptor.getValue(), documentCaptor.getValue().getObjectKey());
    }

    @Test
    public void shouldReturnResultMappedFromSavedDocument() throws IOException {
        byte[] bytes = pdfWithText(CV_TEXT);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cvDocumentRepository.save(any(CvDocument.class))).thenAnswer(inv -> {
            CvDocument document = inv.getArgument(0);
            ReflectionTestUtils.setField(document, "id", SAVED_ID);
            return document;
        });

        UploadResult result = service.execute(command(bytes));

        assertEquals(SAVED_ID, result.id());
        assertEquals(ORIGINAL_NAME, result.originalName());
        assertEquals(PDF, result.contentType());
        assertEquals(ExtractionStatus.DONE, result.extractionStatus());
    }

    @Test
    public void shouldMarkExtractionFailedWhenPdfHasNoText() throws IOException {
        byte[] bytes = pdfWithText(null);
        stubUserAndSave();

        UploadResult result = service.execute(command(bytes));

        verify(cvDocumentRepository).save(documentCaptor.capture());
        assertEquals(ExtractionStatus.FAILED, documentCaptor.getValue().getExtractionStatus());
        assertNull(documentCaptor.getValue().getExtractedText());
        assertEquals(ExtractionStatus.FAILED, result.extractionStatus());
        verify(storage).store(any(), any(), anyLong(), any());
    }

    @Test
    public void shouldMarkExtractionFailedWhenPdfIsCorrupted() {
        byte[] bytes = "%PDF-1.7 this is not really a pdf".getBytes(StandardCharsets.UTF_8);
        stubUserAndSave();

        UploadResult result = service.execute(command(bytes));

        verify(cvDocumentRepository).save(documentCaptor.capture());
        assertEquals(ExtractionStatus.FAILED, documentCaptor.getValue().getExtractionStatus());
        assertEquals(ExtractionStatus.FAILED, result.extractionStatus());
        verify(storage).store(any(), any(), anyLong(), any());
    }

    @Test
    public void shouldNotSaveDocumentWhenStoringFileFails() throws IOException {
        byte[] bytes = pdfWithText(CV_TEXT);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        doThrow(new IllegalStateException("storage down")).when(storage).store(any(), any(), anyLong(), any());

        assertThrows(IllegalStateException.class, () -> service.execute(command(bytes)));

        verify(cvDocumentRepository, never()).save(any());
        verify(storage, never()).delete(any());
    }

    @Test
    public void shouldDeleteStoredObjectAndRethrowWhenSaveFails() throws IOException {
        byte[] bytes = pdfWithText(CV_TEXT);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cvDocumentRepository.save(any(CvDocument.class))).thenThrow(new IllegalStateException("db down"));

        assertThrows(IllegalStateException.class, () -> service.execute(command(bytes)));

        verify(storage).store(keyCaptor.capture(), any(), anyLong(), any());
        verify(storage).delete(keyCaptor.getValue());
    }

    @Test
    public void shouldThrowUserNotFoundAndNotTouchStorage() throws IOException {
        byte[] bytes = pdfWithText(CV_TEXT);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.execute(command(bytes)));

        verifyNoInteractions(storage, cvDocumentRepository);
    }

    @Test
    public void shouldRejectFileWithoutPdfHeader() {
        byte[] bytes = "just some text, not a pdf".getBytes(StandardCharsets.UTF_8);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> service.execute(command(bytes)));

        verifyNoInteractions(storage, cvDocumentRepository);
    }

    @Test
    public void shouldRejectNonPdfContentType() {
        UploadCvCommand command = new UploadCvCommand(USER_ID, "cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 10, new ByteArrayInputStream(new byte[10]));

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));

        verifyNoInteractions(userRepository, storage, cvDocumentRepository);
    }

    @Test
    public void shouldRejectNullContentType() {
        UploadCvCommand command = new UploadCvCommand(USER_ID, ORIGINAL_NAME, null, 10, new ByteArrayInputStream(new byte[10]));

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));

        verifyNoInteractions(userRepository, storage, cvDocumentRepository);
    }

    @Test
    public void shouldRejectEmptyFile() {
        UploadCvCommand command = new UploadCvCommand(USER_ID, ORIGINAL_NAME, PDF, 0, new ByteArrayInputStream(new byte[0]));

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));

        verifyNoInteractions(userRepository, storage, cvDocumentRepository);
    }

    @Test
    public void shouldRejectNullContent() {
        UploadCvCommand command = new UploadCvCommand(USER_ID, ORIGINAL_NAME, PDF, 10, null);

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));

        verifyNoInteractions(userRepository, storage, cvDocumentRepository);
    }

    @Test
    public void shouldRejectNullUserId() {
        UploadCvCommand command = new UploadCvCommand(null, ORIGINAL_NAME, PDF, 10, new ByteArrayInputStream(new byte[10]));

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));

        verifyNoInteractions(userRepository, storage, cvDocumentRepository);
    }

    @Test
    public void shouldRejectNullCommand() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null));

        verifyNoInteractions(userRepository, storage, cvDocumentRepository);
    }
}
