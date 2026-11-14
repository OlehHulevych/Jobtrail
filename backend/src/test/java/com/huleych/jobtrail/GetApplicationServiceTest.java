package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.services.GetApplicationService;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetApplicationServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    JobApplicationRepository repository;
    JobApplication jobApplication;
    GetApplicationService service;

    @BeforeEach
    public void init() {
        service = new GetApplicationService(repository);
        jobApplication = new JobApplication(OWNER_ID, "Acme", "Java Developer", "https://acme.example/jobs/1", "Build backend services");
    }

    @Test
    public void shouldReturnResponseWhenCallerIsOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        JobApplicationResponse response = service.execute(OWNER_ID, APPLICATION_ID);

        assertEquals("Acme", response.company());
        assertEquals("Java Developer", response.position());
        assertEquals("https://acme.example/jobs/1", response.url());
        assertEquals("Build backend services", response.jobDescription());
        assertEquals(ApplicationStatus.WISHLIST, response.status());
        assertNull(response.appliedAt());
    }

    @Test
    public void shouldThrowApplicationNotFoundWhenApplicationDoesNotExist() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.execute(OWNER_ID, APPLICATION_ID));
    }

    @Test
    public void shouldThrowNotOwnerWhenCallerIsNotOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(NotApplicationOwnerException.class, () -> service.execute(OTHER_OWNER_ID, APPLICATION_ID));
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
