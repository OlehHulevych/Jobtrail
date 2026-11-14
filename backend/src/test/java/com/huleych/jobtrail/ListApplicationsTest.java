package com.huleych.jobtrail;


import com.huleych.jobtrail.tracking.application.queries.ListApplicationsQuery;
import com.huleych.jobtrail.tracking.application.services.ListApplications;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListApplicationsTest {
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID OWNER_WITHOUT_APPLICATIONS_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Mock
    JobApplicationRepository jobApplicationRepository;
    ListApplications listApplications;
    JobApplication firstApplication;
    JobApplication secondApplication;

    @BeforeEach
    public void init(){
        listApplications = new ListApplications(jobApplicationRepository);
         firstApplication = new JobApplication(OWNER_ID, "Acme", "Java Developer", null, null);
         secondApplication = new JobApplication(OWNER_ID, "Globex", "Backend Engineer", null, null);
    }

    @Test
    public void shouldReturnAllApplicationsOfOwnerWhenStatusIsNull(){
        when(jobApplicationRepository.findAllByOwnerId(OWNER_ID)).thenReturn(List.of(firstApplication, secondApplication));
        List<JobApplication> result = listApplications.execute(new ListApplicationsQuery(OWNER_ID, null));
        assertEquals(2, result.size());
        assertEquals(List.of(firstApplication, secondApplication), result);
        verify(jobApplicationRepository, never()).findAllByOwnerIdAndStatus(any(), any());

    }

    @Test
    public void shouldFilterByStatusWhenStatusIsGiven(){
        firstApplication.apply(LocalDate.now());
        when(jobApplicationRepository.findAllByOwnerIdAndStatus(OWNER_ID, ApplicationStatus.APPLIED)).thenReturn(List.of(firstApplication));
        List<JobApplication> result = listApplications.execute(new ListApplicationsQuery(OWNER_ID, ApplicationStatus.APPLIED));
        assertEquals(1, result.size());
        assertEquals(List.of(firstApplication), result);
        verify(jobApplicationRepository, never()).findAllByOwnerId(any());
    }

    @Test
    public void shouldReturnEmptyListWhenOwnerHasNoApplications(){
        when(jobApplicationRepository.findAllByOwnerId(OWNER_WITHOUT_APPLICATIONS_ID)).thenReturn(List.of());
        List<JobApplication> result = listApplications.execute(new ListApplicationsQuery(OWNER_WITHOUT_APPLICATIONS_ID, null));
        assertEquals(0, result.size());
        assertEquals(List.of(), result);
        verify(jobApplicationRepository, never()).findAllByOwnerIdAndStatus(any(), any());
    }

    @Test
    public void shouldRejectNullOwnerId(){
        assertThrows(IllegalArgumentException.class, ()->listApplications.execute(new ListApplicationsQuery(null,null)));
        verify(jobApplicationRepository, never()).findAllByOwnerIdAndStatus(any(), any());
        verify(jobApplicationRepository, never()).findAllByOwnerId(any());
    }

    @Test
    public void shouldRejectNullQuery(){
        assertThrows(IllegalArgumentException.class, ()->listApplications.execute(null));
        verify(jobApplicationRepository, never()).findAllByOwnerIdAndStatus(any(), any());
        verify(jobApplicationRepository, never()).findAllByOwnerId(any());
    }
}
