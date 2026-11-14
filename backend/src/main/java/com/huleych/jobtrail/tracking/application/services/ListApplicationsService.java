package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.application.queries.ListApplicationsQuery;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.web.JobApplicationResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListApplicationsService {
    private final ListApplications listApplications;

    public ListApplicationsService(ListApplications listApplications) {
        this.listApplications = listApplications;
    }

    public List<JobApplicationResponse> execute(UUID userId, ApplicationStatus status) {
        if (userId == null) throw new IllegalArgumentException("User id is required");
        return listApplications.execute(new ListApplicationsQuery(userId, status)).stream()
                .map(application -> new JobApplicationResponse(
                        application.getId(),
                        application.getCompany(),
                        application.getPosition(),
                        application.getUrl(),
                        application.getJobDescription(),
                        application.getStatus(),
                        application.getAppliedAt(),
                        application.getCreatedAt()))
                .toList();
    }
}
