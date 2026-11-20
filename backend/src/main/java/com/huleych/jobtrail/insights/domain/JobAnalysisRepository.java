package com.huleych.jobtrail.insights.domain;

import java.util.Optional;
import java.util.UUID;

public interface JobAnalysisRepository {

    JobAnalysis save(JobAnalysis jobAnalysis);
    Optional<JobAnalysis> findByApplicationId(UUID applicationId);

}
