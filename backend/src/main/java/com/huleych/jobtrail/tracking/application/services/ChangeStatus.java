package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.shared.Config;
import com.huleych.jobtrail.tracking.application.commands.ChangeStatusCommand;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public class ChangeStatus {
    private final JobApplicationRepository repository;
    private final Clock clock;

    public ChangeStatus(JobApplicationRepository repository, Clock clock){
        this.repository = repository;
        this.clock = clock;
    }

    public JobApplication execute(ChangeStatusCommand command){
        JobApplication application = repository.findById(command.applicationId()).orElse(null);
        if(application==null) throw new ApplicationNotFoundException(command.applicationId());
        if(!Objects.equals(application.getOwnerId(), command.ownerId())) throw new NotApplicationOwnerException(command.ownerId(), command.applicationId());
        if(command.status()== ApplicationStatus.APPLIED){
            application.apply(LocalDate.now(clock));
            return repository.save(application);
        }
        application.moveTo(command.status());
        return repository.save(application);


    }
}
