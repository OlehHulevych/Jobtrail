package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetApplicationService {
    private final JobApplicationRepository repository;

    public GetApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse execute(UUID userId, UUID applicationId) {
        if (userId == null) throw new IllegalArgumentException("User id is required");
        if (applicationId == null) throw new IllegalArgumentException("Application id is required");
        JobApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
        if (!application.getOwnerId().equals(userId)) throw new NotApplicationOwnerException(userId, applicationId);
        return new JobApplicationResponse(
                application.getId(),
                application.getCompany(),
                application.getPosition(),
                application.getUrl(),
                application.getJobDescription(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getCreatedAt());
    }
}
