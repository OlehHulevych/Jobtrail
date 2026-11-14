package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.domain.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundException;
import com.huleych.jobtrail.tracking.application.services.CreateApplication;
import com.huleych.jobtrail.tracking.application.services.CreateApplicationService;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.web.CreateApplicationRequest;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateApplicationServiceTest {
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Mock
    UserRepository userRepository;
    FakeJobApplicationRepository repository;
    CreateApplicationService service;

    @BeforeEach
    public void init() {
        repository = new FakeJobApplicationRepository();
        service = new CreateApplicationService(new CreateApplication(repository), userRepository);
    }

    @Test
    public void shouldCreateApplicationAndReturnResponseWhenUserExists() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        CreateApplicationRequest request = new CreateApplicationRequest("Acme", "Java Developer", "https://acme.example/jobs/1", "Build backend services");

        JobApplicationResponse response = service.execute(USER_ID, request);

        assertEquals(1, repository.storage.size());
        assertEquals(USER_ID, repository.storage.getFirst().getOwnerId());
        assertEquals("Acme", response.company());
        assertEquals("Java Developer", response.position());
        assertEquals("https://acme.example/jobs/1", response.url());
        assertEquals("Build backend services", response.jobDescription());
        assertEquals(ApplicationStatus.WISHLIST, response.status());
        assertNull(response.appliedAt());
    }

    @Test
    public void shouldAllowMissingUrlAndDescription() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        JobApplicationResponse response = service.execute(USER_ID, new CreateApplicationRequest("Acme", "Java Developer", null, null));

        assertNull(response.url());
        assertNull(response.jobDescription());
    }

    @Test
    public void shouldThrowUserNotFoundWhenUserDoesNotExist() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> service.execute(USER_ID, new CreateApplicationRequest("Acme", "Java Developer", null, null)));
        assertTrue(repository.storage.isEmpty());
    }

    @Test
    public void shouldRejectNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(USER_ID, null));
        verifyNoInteractions(userRepository);
        assertTrue(repository.storage.isEmpty());
    }

    @Test
    public void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.execute(null, new CreateApplicationRequest("Acme", "Java Developer", null, null)));
        verifyNoInteractions(userRepository);
        assertTrue(repository.storage.isEmpty());
    }
}
