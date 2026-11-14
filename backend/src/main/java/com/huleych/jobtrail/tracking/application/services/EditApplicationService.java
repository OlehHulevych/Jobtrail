package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.application.commands.UpdateJobApplicationCommand;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.web.EditApplicationRequest;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EditApplicationService {
    private final EditApplication editApplication;

    public EditApplicationService(EditApplication editApplication) {
        this.editApplication = editApplication;
    }

    public JobApplicationResponse execute(UUID userId, UUID applicationId, EditApplicationRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required for editing job application");
        if (userId == null) throw new IllegalArgumentException("User id is required");
        if (applicationId == null) throw new IllegalArgumentException("Application id is required");
        JobApplication application = editApplication.execute(new UpdateJobApplicationCommand(
                applicationId, userId, request.company(), request.position(), request.url(), request.jobDescription()));
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
