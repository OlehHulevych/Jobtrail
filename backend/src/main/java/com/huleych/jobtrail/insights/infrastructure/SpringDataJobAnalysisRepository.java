package com.huleych.jobtrail.insights.infrastructure;

import com.huleych.jobtrail.insights.domain.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataJobAnalysisRepository extends JpaRepository<JobAnalysis, UUID> {
    Optional<JobAnalysis> findByApplicationId(UUID applicationId);
}
