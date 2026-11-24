package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.services.DeleteNoteService;
import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import com.huleych.jobtrail.tracking.infrastructure.repository.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteNoteServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID NOTE_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @Mock
    JobApplicationRepository repository;
    JobApplication jobApplication;
    DeleteNoteService service;

    @BeforeEach
    public void init() {
        service = new DeleteNoteService(repository);
        jobApplication = new JobApplication(OWNER_ID, "Acme", "Java Developer", "https://acme.example/jobs/1", "Build backend services");
        jobApplication.addNote("Prepare for HR interview");
        ReflectionTestUtils.setField(jobApplication.getNotes().getFirst(), "id", NOTE_ID);
    }

    @Test
    public void shouldDeleteNoteAndSaveApplicationWhenCallerIsOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        service.execute(OWNER_ID, APPLICATION_ID, NOTE_ID);

        assertTrue(jobApplication.getNotes().isEmpty());
        verify(repository).save(jobApplication);
    }

    @Test
    public void shouldThrowResourceNotFoundWhenNoteDoesNotExist() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(ResourceNotFoundException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, UUID.randomUUID()));
        assertEquals(1, jobApplication.getNotes().size());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"REJECTED", "WITHDRAWN"})
    public void shouldRejectDeleteWhenApplicationIsClosed(ApplicationStatus closedStatus) {
        jobApplication.apply(LocalDate.now());
        jobApplication.moveTo(closedStatus);
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(IllegalStateException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, NOTE_ID));
        assertEquals(1, jobApplication.getNotes().size());
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldThrowApplicationNotFoundWhenApplicationDoesNotExist() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, NOTE_ID));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldThrowNotOwnerWhenCallerIsNotOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(NotApplicationOwnerException.class, () -> service.execute(OTHER_OWNER_ID, APPLICATION_ID, NOTE_ID));
        assertEquals(1, jobApplication.getNotes().size());
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null, APPLICATION_ID, NOTE_ID));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullApplicationId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, null, NOTE_ID));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullNoteId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, null));
        verifyNoInteractions(repository);
    }
}