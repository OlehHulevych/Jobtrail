package com.huleych.jobtrail.tracking.web;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;

public record ChangeStatusRequest(ApplicationStatus status) {
}