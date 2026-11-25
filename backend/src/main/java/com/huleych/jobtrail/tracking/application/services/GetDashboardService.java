package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.web.DashboardResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class GetDashboardService {
    private final GetDashboard getDashboard;

    public GetDashboardService(GetDashboard getDashboard) {
        this.getDashboard = getDashboard;
    }

    public DashboardResponse execute(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("User id is required");
        Map<ApplicationStatus, Long> byStatus = getDashboard.execute(userId);
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return new DashboardResponse(total, byStatus);
    }
}
