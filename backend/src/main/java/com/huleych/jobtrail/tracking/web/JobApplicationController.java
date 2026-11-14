package com.huleych.jobtrail.tracking.web;

import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.tracking.application.services.CreateApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {
    private final CreateApplicationService createApplicationService;

    public JobApplicationController(CreateApplicationService createApplicationService) {
        this.createApplicationService = createApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponse create(@AuthenticationPrincipal User user, @RequestBody CreateApplicationRequest request) {
        return createApplicationService.execute(user.getId(), request);
    }
}
