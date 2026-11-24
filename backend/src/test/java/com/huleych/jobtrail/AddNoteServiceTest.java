package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.services.AddNoteService;
import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import com.huleych.jobtrail.tracking.infrastructure.repository.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.web.AddNoteRequest;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddNoteServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    JobApplicationRepository repository;
    JobApplication jobApplication;
    AddNoteService service;

    @BeforeEach
    public void init() {
        service = new AddNoteService(repository);
        jobApplication = new JobApplication(OWNER_ID, "Acme", "Java Developer", "https://acme.example/jobs/1", "Build backend services");
    }

    @Test
    public void shouldAddNoteSaveApplicationAndReturnResponse() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = service.execute(OWNER_ID, APPLICATION_ID, new AddNoteRequest("Prepare for HR interview"));

        verify(repository).save(jobApplication);
        assertEquals(1, jobApplication.getNotes().size());
        assertEquals("Prepare for HR interview", jobApplication.getNotes().getFirst().getValue());
        assertEquals("Acme", response.company());
        assertEquals(ApplicationStatus.WISHLIST, response.status());
    }

    @Test
    public void shouldRejectBlankNote() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, new AddNoteRequest(" ")));
        verify(repository, never()).save(any());
        assertTrue(jobApplication.getNotes().isEmpty());
    }

    @Test
    public void shouldRejectNullNoteValue() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, new AddNoteRequest(null)));
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"REJECTED", "WITHDRAWN"})
    public void shouldRejectNoteWhenApplicationIsClosed(ApplicationStatus closedStatus) {
        jobApplication.apply(LocalDate.now());
        jobApplication.moveTo(closedStatus);
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(IllegalStateException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, new AddNoteRequest("Too late")));
        verify(repository, never()).save(any());
        assertTrue(jobApplication.getNotes().isEmpty());
    }

    @Test
    public void shouldThrowApplicationNotFoundWhenApplicationDoesNotExist() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, new AddNoteRequest("Note")));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldThrowNotOwnerWhenCallerIsNotOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(NotApplicationOwnerException.class, () -> service.execute(OTHER_OWNER_ID, APPLICATION_ID, new AddNoteRequest("Note")));
        verify(repository, never()).save(any());
        assertTrue(jobApplication.getNotes().isEmpty());
    }

    @Test
    public void shouldRejectNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, null));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null, APPLICATION_ID, new AddNoteRequest("Note")));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullApplicationId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, null, new AddNoteRequest("Note")));
        verifyNoInteractions(repository);
    }
}
