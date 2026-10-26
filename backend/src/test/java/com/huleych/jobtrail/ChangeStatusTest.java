package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.commands.ChangeStatusCommand;
import com.huleych.jobtrail.tracking.application.services.ChangeStatus;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.InvalidStatusTransitionException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import net.datafaker.Faker;
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
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChangeStatusTest {
    @Mock
    JobApplicationRepository repository;
    ChangeStatus changeStatus;
    JobApplication application;
    Clock clock;
    Faker faker;


    @BeforeEach
    public void init(){
        faker = new Faker(new Random(42));
        clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);
        changeStatus = new ChangeStatus(repository, clock);
        application = new JobApplication(5L, faker.company().name(), faker.job().position(), "google.com", faker.job().keySkills() );
    }

    @Test
    public void shouldMoveApplicationToNewStatus(){
        when(repository.findById(10L)).thenReturn(Optional.of(application));
        changeStatus.execute(new ChangeStatusCommand(10L,5L, ApplicationStatus.WITHDRAWN));
        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
        verify(repository).save(application);
    }

    @Test
    public void shouldApplyWithDateFromClock(){
        when(repository.findById(10L)).thenReturn(Optional.of(application));
        changeStatus.execute(new ChangeStatusCommand(10L, 5L, ApplicationStatus.APPLIED));
        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertEquals(LocalDate.of(2026, 1, 15), application.getAppliedAt());
        verify(repository).save(application);
    }

    @Test
    public void shouldRejectInvalidTransition(){
        when(repository.findById(10L)).thenReturn(Optional.of(application));
        assertThrows(InvalidStatusTransitionException.class, ()->changeStatus.execute(new ChangeStatusCommand(10L, 5L, ApplicationStatus.OFFER)));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRejectWhenApplicationNotFound(){
        when(repository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ApplicationNotFoundException.class, ()->changeStatus.execute(new ChangeStatusCommand(10L, 5L, ApplicationStatus.APPLIED)));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRejectWhenCallerIsNotOwner(){
        when(repository.findById(10L)).thenReturn(Optional.of(application));
        assertThrows(NotApplicationOwnerException.class, ()->changeStatus.execute(new ChangeStatusCommand(10L, 3L, ApplicationStatus.APPLIED)));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRejectNullCommand() {
        assertThrows(IllegalArgumentException.class, () -> changeStatus.execute(null));
        verifyNoInteractions(repository);
    }
}
