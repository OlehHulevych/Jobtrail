package com.huleych.jobtrail.tracking.web;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;

import java.util.Map;

public record DashboardResponse(long total, Map<ApplicationStatus, Long> byStatus) {
}
