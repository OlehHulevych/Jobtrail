package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import com.huleych.jobtrail.tracking.web.AddNoteRequest;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddNoteService {
    private final JobApplicationRepository repository;

    public AddNoteService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public JobApplicationResponse execute(UUID userId, UUID applicationId, AddNoteRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required for adding note");
        if (userId == null) throw new IllegalArgumentException("User id is required");
        if (applicationId == null) throw new IllegalArgumentException("Application id is required");
        JobApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
        if (!application.getOwnerId().equals(userId)) throw new NotApplicationOwnerException(userId, applicationId);
        application.addNote(request.value());
        JobApplication saved = repository.save(application);
        return new JobApplicationResponse(
                saved.getId(),
                saved.getCompany(),
                saved.getPosition(),
                saved.getUrl(),
                saved.getJobDescription(),
                saved.getStatus(),
                saved.getAppliedAt(),
                saved.getCreatedAt());
    }
}