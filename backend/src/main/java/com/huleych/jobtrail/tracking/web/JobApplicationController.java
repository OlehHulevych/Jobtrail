package com.huleych.jobtrail.tracking.web;

import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.tracking.application.services.CreateApplicationService;
import com.huleych.jobtrail.tracking.application.services.EditApplicationService;
import com.huleych.jobtrail.tracking.application.services.GetApplicationService;
import com.huleych.jobtrail.tracking.application.services.ListApplicationsService;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {
    private final CreateApplicationService createApplicationService;
    private final EditApplicationService editApplicationService;

    private final ListApplicationsService listApplicationsService;
    private final GetApplicationService getApplicationService;

    public JobApplicationController(CreateApplicationService createApplicationService, EditApplicationService editApplicationService, ListApplicationsService listApplicationsService, GetApplicationService getApplicationService) {
        this.createApplicationService = createApplicationService;
        this.editApplicationService = editApplicationService;
        this.listApplicationsService = listApplicationsService;
        this.getApplicationService = getApplicationService;
    }

    @GetMapping("/{id}")
    public JobApplicationResponse get(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return getApplicationService.execute(user.getId(), id);
    }

    @GetMapping
    public List<JobApplicationResponse> list(@AuthenticationPrincipal User user, @RequestParam(required = false) ApplicationStatus status) {
        return listApplicationsService.execute(user.getId(), status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponse create(@AuthenticationPrincipal User user, @RequestBody CreateApplicationRequest request) {
        return createApplicationService.execute(user.getId(), request);
    }

    @PutMapping("/{id}")
    public JobApplicationResponse edit(@AuthenticationPrincipal User user, @PathVariable UUID id, @RequestBody EditApplicationRequest request) {
        return editApplicationService.execute(user.getId(), id, request);
    }
}
