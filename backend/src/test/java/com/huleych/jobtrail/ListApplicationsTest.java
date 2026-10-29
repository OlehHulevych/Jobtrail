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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListApplicationsTest {

    @Mock
    JobApplicationRepository jobApplicationRepository;
    ListApplications listApplications;
    JobApplication firstApplication;
    JobApplication secondApplication;

    @BeforeEach
    public void init(){
        listApplications = new ListApplications(jobApplicationRepository);
         firstApplication = new JobApplication(5L, "Acme", "Java Developer", null, null);
         secondApplication = new JobApplication(5L, "Globex", "Backend Engineer", null, null);
    }

    @Test
    public void shouldReturnAllApplicationsOfOwnerWhenStatusIsNull(){
        when(jobApplicationRepository.findAllByOwnerId(5L)).thenReturn(List.of(firstApplication, secondApplication));
        List<JobApplication> result = listApplications.execute(new ListApplicationsQuery(5L, null));
        assertEquals(2, result.size());
        assertEquals(List.of(firstApplication, secondApplication), result);
        verify(jobApplicationRepository, never()).findAllByOwnerIdAndStatus(any(), any());

    }

    @Test
    public void shouldFilterByStatusWhenStatusIsGiven(){
        firstApplication.apply(LocalDate.now());
        when(jobApplicationRepository.findAllByOwnerIdAndStatus(5L, ApplicationStatus.APPLIED)).thenReturn(List.of(firstApplication));
        List<JobApplication> result = listApplications.execute(new ListApplicationsQuery(5L, ApplicationStatus.APPLIED));
        assertEquals(1, result.size());
        assertEquals(List.of(firstApplication), result);
        verify(jobApplicationRepository, never()).findAllByOwnerId(any());
    }

    @Test
    public void shouldReturnEmptyListWhenOwnerHasNoApplications(){
        when(jobApplicationRepository.findAllByOwnerId(4L)).thenReturn(List.of());
        List<JobApplication> result = listApplications.execute(new ListApplicationsQuery(4L, null));
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
