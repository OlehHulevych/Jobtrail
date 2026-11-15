package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.application.commands.ChangeStatusCommand;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.web.ChangeStatusRequest;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChangeStatusService {
    private final ChangeStatus changeStatus;

    public ChangeStatusService(ChangeStatus changeStatus) {
        this.changeStatus = changeStatus;
    }

    public JobApplicationResponse execute(UUID userId, UUID applicationId, ChangeStatusRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required for changing status");
        if (userId == null) throw new IllegalArgumentException("User id is required");
        if (applicationId == null) throw new IllegalArgumentException("Application id is required");
        JobApplication application = changeStatus.execute(new ChangeStatusCommand(applicationId, userId, request.status()));
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