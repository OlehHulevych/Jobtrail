package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.application.commands.CreateApplicationCommand;
import com.huleych.jobtrail.tracking.application.services.CreateApplication;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void ShouldSaveApplicationWhenCommandIsValid(){
        CreateApplicationCommand command = new CreateApplicationCommand(5L,faker.company().logo().toLowerCase(),faker.job().position(), "google.com", faker.job().keySkills());
        JobApplication jobApplication = createApplication.execute(command);
        assertEquals(command.company(),jobApplication.getCompany());
        assertEquals(command.position(),jobApplication.getPosition());
        assertEquals(command.ownerId(), jobApplication.getOwnerId());
    }
}
