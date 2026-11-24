package com.huleych.jobtrail.insights.infrastructure.repository;

import com.huleych.jobtrail.insights.domain.entity.JobAnalysis;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaJobAnalysisRepository implements JobAnalysisRepository {
    private final SpringDataJobAnalysisRepository repository;

    public JpaJobAnalysisRepository(SpringDataJobAnalysisRepository repository){
        this.repository = repository;
    }

    @Override
    public JobAnalysis save(JobAnalysis jobAnalysis) {
        return repository.save(jobAnalysis);
    }

    @Override
    public Optional<JobAnalysis> findByApplicationId(UUID applicationId) {
        return repository.findByApplicationId(applicationId);
    }
}
