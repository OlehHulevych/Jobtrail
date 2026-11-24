package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import com.huleych.jobtrail.tracking.infrastructure.repository.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteNoteService {
    private final JobApplicationRepository repository;

    public DeleteNoteService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID userId, UUID applicationId, UUID noteId) {
        if (userId == null) throw new IllegalArgumentException("User id is required");
        if (applicationId == null) throw new IllegalArgumentException("Application id is required");
        if (noteId == null) throw new IllegalArgumentException("Note id is required");
        JobApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
        if (!application.getOwnerId().equals(userId)) throw new NotApplicationOwnerException(userId, applicationId);
        application.deleteNote(noteId);
        repository.save(application);
    }
}