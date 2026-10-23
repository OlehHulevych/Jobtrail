package com.huleych.jobtrail.tracking.application.services;


import com.huleych.jobtrail.tracking.application.commands.CreateApplicationCommand;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateApplication {
    private final JobApplicationRepository repository;

    public CreateApplication(JobApplicationRepository repository){
        this.repository = repository;
    }

    @Transactional
    public JobApplication execute(CreateApplicationCommand createApplicationCommand){
        if(createApplicationCommand==null) throw new IllegalArgumentException("Request body is required for creating job application is required");
        JobApplication newJobApplication = new JobApplication(createApplicationCommand.ownerId(),createApplicationCommand.company(),createApplicationCommand.position(),createApplicationCommand.url(),createApplicationCommand.jobDescription());
        return repository.save(newJobApplication);
    }



}
