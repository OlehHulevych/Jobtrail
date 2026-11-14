package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.services.ListApplications;
import com.huleych.jobtrail.tracking.application.services.ListApplicationsService;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListApplicationsServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Mock
    JobApplicationRepository repository;
    ListApplicationsService service;
    JobApplication firstApplication;
    JobApplication secondApplication;

    @BeforeEach
    public void init() {
        service = new ListApplicationsService(new ListApplications(repository));
        firstApplication = new JobApplication(OWNER_ID, "Acme", "Java Developer", "https://acme.example/jobs/1", "Build backend services");
        secondApplication = new JobApplication(OWNER_ID, "Globex", "Backend Engineer", null, null);
    }

    @Test
    public void shouldReturnAllApplicationsAsResponsesWhenStatusIsNull() {
        when(repository.findAllByOwnerId(OWNER_ID)).thenReturn(List.of(firstApplication, secondApplication));

        List<JobApplicationResponse> result = service.execute(OWNER_ID, null);

        assertEquals(2, result.size());
        assertEquals("Acme", result.get(0).company());
        assertEquals("Java Developer", result.get(0).position());
        assertEquals("https://acme.example/jobs/1", result.get(0).url());
        assertEquals("Build backend services", result.get(0).jobDescription());
        assertEquals(ApplicationStatus.WISHLIST, result.get(0).status());
        assertEquals("Globex", result.get(1).company());
        assertNull(result.get(1).url());
        verify(repository, never()).findAllByOwnerIdAndStatus(any(), any());
    }

    @Test
    public void shouldFilterByStatusWhenStatusIsGiven() {
        LocalDate today = LocalDate.now();
        firstApplication.apply(today);
        when(repository.findAllByOwnerIdAndStatus(OWNER_ID, ApplicationStatus.APPLIED)).thenReturn(List.of(firstApplication));

        List<JobApplicationResponse> result = service.execute(OWNER_ID, ApplicationStatus.APPLIED);

        assertEquals(1, result.size());
        assertEquals("Acme", result.getFirst().company());
        assertEquals(ApplicationStatus.APPLIED, result.getFirst().status());
        assertEquals(today, result.getFirst().appliedAt());
        verify(repository, never()).findAllByOwnerId(any());
    }

    @Test
    public void shouldReturnEmptyListWhenOwnerHasNoApplications() {
        when(repository.findAllByOwnerId(OWNER_ID)).thenReturn(List.of());

        assertTrue(service.execute(OWNER_ID, null).isEmpty());
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(null, null));
        verifyNoInteractions(repository);
    }
}
