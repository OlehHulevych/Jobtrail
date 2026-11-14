package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.services.EditApplication;
import com.huleych.jobtrail.tracking.application.services.EditApplicationService;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.web.EditApplicationRequest;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EditApplicationServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    JobApplicationRepository repository;
    JobApplication jobApplication;
    EditApplicationService service;

    @BeforeEach
    public void init() {
        service = new EditApplicationService(new EditApplication(repository));
        jobApplication = new JobApplication(OWNER_ID, "Acme", "Java Developer", "https://acme.example/jobs/1", "Build backend services");
    }

    @Test
    public void shouldEditApplicationAndReturnResponseWhenCallerIsOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = service.execute(OWNER_ID, APPLICATION_ID,
                new EditApplicationRequest("Globex", "Backend Engineer", "http://globex.example", "New description"));

        verify(repository).save(jobApplication);
        assertEquals("Globex", response.company());
        assertEquals("Backend Engineer", response.position());
        assertEquals("http://globex.example", response.url());
        assertEquals("New description", response.jobDescription());
        assertEquals(ApplicationStatus.WISHLIST, response.status());
    }

    @Test
    public void shouldThrowApplicationNotFoundWhenApplicationDoesNotExist() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.execute(OWNER_ID, APPLICATION_ID,
                new EditApplicationRequest("Globex", "Backend Engineer", null, null)));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldThrowNotOwnerWhenCallerIsNotOwner() {
        when(repository.findById(APPLICATION_ID)).thenReturn(Optional.of(jobApplication));

        assertThrows(NotApplicationOwnerException.class, () -> service.execute(OTHER_OWNER_ID, APPLICATION_ID,
                new EditApplicationRequest("Globex", "Backend Engineer", null, null)));
        verify(repository, never()).save(any());
        assertEquals("Acme", jobApplication.getCompany());
    }

    @Test
    public void shouldRejectNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, APPLICATION_ID, null));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null, APPLICATION_ID,
                new EditApplicationRequest("Globex", "Backend Engineer", null, null)));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldRejectNullApplicationId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(OWNER_ID, null,
                new EditApplicationRequest("Globex", "Backend Engineer", null, null)));
        verifyNoInteractions(repository);
    }
}
