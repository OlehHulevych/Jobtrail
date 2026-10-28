package com.huleych.jobtrail.tracking.application.queries;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;

public record ListApplicationsQuery(Long ownerId, ApplicationStatus status) {
}
