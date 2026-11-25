package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.infrastructure.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GetDashboard {
    private final JobApplicationRepository repository;

    public GetDashboard(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<ApplicationStatus, Long> execute(UUID ownerId) {
        if (ownerId == null) throw new IllegalArgumentException("owner id is required");
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status, 0L);
        }
        counts.putAll(repository.countByStatus(ownerId));
        return counts;
    }
}
