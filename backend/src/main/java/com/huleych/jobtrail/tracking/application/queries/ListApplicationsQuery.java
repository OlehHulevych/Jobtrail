package com.huleych.jobtrail.tracking.application.queries;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;

import java.util.UUID;

public record ListApplicationsQuery(UUID ownerId, ApplicationStatus status) {
}
