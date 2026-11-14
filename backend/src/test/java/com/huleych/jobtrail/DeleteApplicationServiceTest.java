package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.services.DeleteApplicationService;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteApplicationServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    JobApplicationRepository repository;
    JobApplication jobApplication;
    DeleteApplicationService service;

    @BeforeEach
    public void init() {
        service = new DeleteApplicationService(repository);
        jobApplication = new JobApplication(OWNER_ID, "Acme", "Java Developer", "https://acme.example/jobs/1", "Build backend services");
    }

    @Test
    public void shouldDeleteApplicationWhenCallerIsOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        service.execute(OWNER_ID, APPLICATION_ID);

        verify(repository).delete(jobApplication);
    }

    @Test
    public void shouldThrowApplicationNotFoundWhenApplicationDoesNotExist() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.execute(OWNER_ID, APPLICATION_ID));
        verify(repository, never()).delete(any());
    }

    @Test
    public void shouldThrowNotOwnerWhenCallerIsNotOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(NotApplicationOwnerException.class, () -> service.execute(OTHER_OWNER_ID, APPLICATION_ID));
        verify(repository, never()).delete(any());
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null, APPLICATION_ID));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullApplicationId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, null));
        verifyNoInteractions(repository);
    }
}
