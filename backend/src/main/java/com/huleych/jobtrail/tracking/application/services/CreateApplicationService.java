package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.identity.domain.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundException;
import com.huleych.jobtrail.tracking.application.commands.CreateApplicationCommand;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.web.CreateApplicationRequest;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateApplicationService {
    private final CreateApplication createApplication;
    private final UserRepository userRepository;

    public CreateApplicationService(CreateApplication createApplication, UserRepository userRepository) {
        this.createApplication = createApplication;
        this.userRepository = userRepository;
    }

    public JobApplicationResponse execute(UUID userId, CreateApplicationRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required for creating job application");
        if (userId == null) throw new IllegalArgumentException("User id is required");
        if (!userRepository.existsById(userId)) throw new UserNotFoundException(userId);
        JobApplication application = createApplication.execute(new CreateApplicationCommand(
                userId, request.company(), request.position(), request.url(), request.jobDescription()));
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
