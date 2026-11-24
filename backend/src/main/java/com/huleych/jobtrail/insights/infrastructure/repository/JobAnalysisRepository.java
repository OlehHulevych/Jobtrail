package com.huleych.jobtrail.insights.infrastructure.repository;

import com.huleych.jobtrail.insights.domain.entity.JobAnalysis;

import java.util.Optional;
import java.util.UUID;

public interface JobAnalysisRepository {

    JobAnalysis save(JobAnalysis jobAnalysis);
    Optional<JobAnalysis> findByApplicationId(UUID applicationId);

}
