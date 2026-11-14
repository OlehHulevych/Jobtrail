package com.huleych.jobtrail.tracking.web;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobApplicationResponse(
        UUID id,
        String company,
        String position,
        String url,
        String jobDescription,
        ApplicationStatus status,
        LocalDate appliedAt,
        LocalDateTime createdAt) {
}
