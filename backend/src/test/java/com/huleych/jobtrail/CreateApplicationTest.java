package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.commands.CreateApplicationCommand;
import com.huleych.jobtrail.tracking.application.services.CreateApplication;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CreateApplicationTest {
    private  FakeJobApplicationRepository repository;
    private  CreateApplication createApplication;
    private Faker faker;
    @BeforeEach
    void init(){
        repository = new FakeJobApplicationRepository();
        createApplication = new CreateApplication(repository);
        faker = new Faker(new Random(42));
    }
    @Test
    public void shouldSaveApplicationWhenCommandIsValid(){
        CreateApplicationCommand command = new CreateApplicationCommand(5L,faker.company().logo().toLowerCase(),faker.job().position(), "google.com", faker.job().keySkills());
        JobApplication jobApplication = createApplication.execute(command);
        JobApplication storedApplication = repository.storage.getFirst();
        assertEquals(1,repository.storage.size());
        assertSame(jobApplication, storedApplication);
    }
    @Test
    public void shouldMapAllFieldsFromCommand(){
        CreateApplicationCommand command = new CreateApplicationCommand(5L,faker.company().logo().toLowerCase(),faker.job().position(), "google.com", faker.job().keySkills());
        JobApplication jobApplication = createApplication.execute(command);
        assertEquals(command.company(),jobApplication.getCompany());
        assertEquals(command.position(),jobApplication.getPosition());
        assertEquals(command.ownerId(), jobApplication.getOwnerId());
        assertEquals(command.jobDescription(), jobApplication.getJobDescription());
        assertEquals(command.url(), jobApplication.getUrl());
    }
    @Test
    public void shouldStartInWishList(){
        CreateApplicationCommand command = new CreateApplicationCommand(5L,faker.company().logo().toLowerCase(),faker.job().position(), "google.com", faker.job().keySkills());
        JobApplication jobApplication = createApplication.execute(command);
        assertEquals(ApplicationStatus.WISHLIST, jobApplication.getStatus());
    }
    @Test
    public void shouldAllowMissingUrlAndDescription(){
        CreateApplicationCommand command = new CreateApplicationCommand(5L,faker.company().logo().toLowerCase(),faker.job().position(), null, null);
        JobApplication jobApplication = createApplication.execute(command);
        assertNull(jobApplication.getUrl());
        assertNull(jobApplication.getJobDescription());

    }
    @Test
    public void shouldRejectBlankCompany(){
        CreateApplicationCommand command = new CreateApplicationCommand(5L,"",faker.job().position(), null, null);
        assertThrows(IllegalArgumentException.class, ()->createApplication.execute(command));

    }
    @Test
    public void shouldRejectBlankPosition(){
        CreateApplicationCommand command = new CreateApplicationCommand(5L,faker.company().logo().toLowerCase(),"", null, null);
        assertThrows(IllegalArgumentException.class, ()->createApplication.execute(command));

    }

    @Test
    public void shouldRejectNullOwner(){
        CreateApplicationCommand command = new CreateApplicationCommand(null,faker.company().logo().toLowerCase(),faker.job().position(), null, null);
        assertThrows(IllegalArgumentException.class, ()->createApplication.execute(command));

    }
    @Test
    public void shouldRejectNullCommand(){
        assertThrows(IllegalArgumentException.class, ()->createApplication.execute(null));

    }
}
