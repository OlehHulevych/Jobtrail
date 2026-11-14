package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteApplicationService {
    private final JobApplicationRepository repository;

    public DeleteApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID userId, UUID applicationId) {
        if (userId == null) throw new IllegalArgumentException("User id is required");
        if (applicationId == null) throw new IllegalArgumentException("Application id is required");
        JobApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
        if (!application.getOwnerId().equals(userId)) throw new NotApplicationOwnerException(userId, applicationId);
        repository.delete(application);
    }
}
