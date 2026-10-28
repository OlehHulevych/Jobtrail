package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.application.commands.UpdateJobApplicationCommand;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditApplication {

    private final JobApplicationRepository repository;
    public EditApplication(JobApplicationRepository repository){
        this.repository = repository;
    }

    @Transactional
    public JobApplication execute(UpdateJobApplicationCommand command){
        if(command == null) throw new IllegalArgumentException("Edit command is required");
        JobApplication jobApplicationForUpdate = repository.findById(command.id()).orElse(null);
        if (jobApplicationForUpdate==null) throw new ApplicationNotFoundException(command.id());
        if(!jobApplicationForUpdate.getOwnerId().equals(command.ownerId())) throw new NotApplicationOwnerException(command.ownerId(), command.id());
        jobApplicationForUpdate.editDetails(command.company(),command.position(),command.url(),command.jobDescription());
        return repository.save(jobApplicationForUpdate);
    }
}
