package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.services.ChangeStatus;
import com.huleych.jobtrail.tracking.application.services.ChangeStatusService;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.InvalidStatusTransitionException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.web.ChangeStatusRequest;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChangeStatusServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    JobApplicationRepository repository;
    JobApplication jobApplication;
    ChangeStatusService service;

    @BeforeEach
    public void init() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);
        service = new ChangeStatusService(new ChangeStatus(repository, clock));
        jobApplication = new JobApplication(OWNER_ID, "Acme", "Java Developer", "https://acme.example/jobs/1", "Build backend services");
    }

    @Test
    public void shouldMoveApplicationToNewStatusAndReturnResponse() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = service.execute(OWNER_ID, APPLICATION_ID, new ChangeStatusRequest(ApplicationStatus.WITHDRAWN));

        assertEquals(ApplicationStatus.WITHDRAWN, response.status());
        assertEquals("Acme", response.company());
        verify(repository).save(jobApplication);
    }

    @Test
    public void shouldSetAppliedDateFromClockWhenMovingToApplied() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = service.execute(OWNER_ID, APPLICATION_ID, new ChangeStatusRequest(ApplicationStatus.APPLIED));

        assertEquals(ApplicationStatus.APPLIED, response.status());
        assertEquals(LocalDate.of(2026, 1, 15), response.appliedAt());
    }

    @Test
    public void shouldRejectInvalidTransition() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.execute(OWNER_ID, APPLICATION_ID, new ChangeStatusRequest(ApplicationStatus.OFFER)));
        verify(repository, never()).save(any());
        assertEquals(ApplicationStatus.WISHLIST, jobApplication.getStatus());
    }

    @Test
    public void shouldThrowApplicationNotFoundWhenApplicationDoesNotExist() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class,
                () -> service.execute(OWNER_ID, APPLICATION_ID, new ChangeStatusRequest(ApplicationStatus.WITHDRAWN)));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldThrowNotOwnerWhenCallerIsNotOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(NotApplicationOwnerException.class,
                () -> service.execute(OTHER_OWNER_ID, APPLICATION_ID, new ChangeStatusRequest(ApplicationStatus.WITHDRAWN)));
        verify(repository, never()).save(any());
        assertEquals(ApplicationStatus.WISHLIST, jobApplication.getStatus());
    }

    @Test
    public void shouldRejectNullStatus() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(IllegalArgumentException.class,
                () -> service.execute(OWNER_ID, APPLICATION_ID, new ChangeStatusRequest(null)));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRejectNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, null));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.execute(null, APPLICATION_ID, new ChangeStatusRequest(ApplicationStatus.WITHDRAWN)));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullApplicationId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.execute(OWNER_ID, null, new ChangeStatusRequest(ApplicationStatus.WITHDRAWN)));
        verifyNoInteractions(repository);
    }
}