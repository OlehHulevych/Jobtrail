package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.commands.UpdateJobApplicationCommand;
import com.huleych.jobtrail.tracking.application.services.EditApplication;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EditApplicationTest {
    @Mock
    JobApplicationRepository repository;
    JobApplication jobApplication;
    EditApplication editApplication;


    @BeforeEach
    public void init(){
        editApplication = new EditApplication(repository);
        jobApplication = new JobApplication(5L, "Acme", "Java Developer","https://acme.example/jobs/1", "Build backend services with Spring Boot");
    }

    @Test
    public void shouldEditDetailsWhenCallerIsOwner(){
        UpdateJobApplicationCommand command = new UpdateJobApplicationCommand(10L, 5L, "Globex", "Backend Engineer", "http://php.com", "The most vulnerable language in 2026");
        when(repository.findById(10L)).thenReturn(Optional.of(jobApplication));
        editApplication.execute(command);
        verify(repository).save(jobApplication);
        assertEquals("Globex", jobApplication.getCompany());
        assertEquals("Backend Engineer", jobApplication.getPosition());

    }

    @Test
    public void shouldRejectWhenCallerIsNotOwner(){
        UpdateJobApplicationCommand command = new UpdateJobApplicationCommand(10L, 3L, "Globex", "Backend Engineer", "http://php.com", "The most vulnerable language in 2026");
        when(repository.findById(10L)).thenReturn(Optional.of(jobApplication));
        assertThrows(NotApplicationOwnerException.class, ()-> editApplication.execute(command));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRejectWhenApplicationNotFound(){
        UpdateJobApplicationCommand command = new UpdateJobApplicationCommand(10L, 3L, "Globex", "Backend Engineer", "http://php.com", "The most vulnerable language in 2026");
        when(repository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ApplicationNotFoundException.class, ()-> editApplication.execute(command));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRejectBlankCompany(){
        UpdateJobApplicationCommand command = new UpdateJobApplicationCommand(10L, 5L, "", "Backend Engineer", "http://php.com", "The most vulnerable language in 2026");
        when(repository.findById(10L)).thenReturn(Optional.of(jobApplication));
        assertThrows(IllegalArgumentException.class, ()-> editApplication.execute(command));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRejectNullCommand(){
        assertThrows(IllegalArgumentException.class, () -> editApplication.execute(null));
        verifyNoInteractions(repository);
    }

    @Test
    public void shouldKeepOldUrlWhenUrlIsNull(){
        UpdateJobApplicationCommand command = new UpdateJobApplicationCommand(10L, 5L, "Globex", "Backend Engineer", null, "The most vulnerable language in 2026");
        when(repository.findById(10L)).thenReturn(Optional.of(jobApplication));
        editApplication.execute(command);
        verify(repository).save(jobApplication);
        assertEquals("Globex", jobApplication.getCompany());
        assertEquals("Backend Engineer", jobApplication.getPosition());
    }



}
