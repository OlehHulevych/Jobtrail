package com.huleych.jobtrail.insights.infrastructure.repository;

import com.huleych.jobtrail.insights.domain.entity.CvMatchAnalysis;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCvMatchAnalysisRepository implements CvMatchAnalysisRepository {
    private final SpringDataCvMatchAnalysisRepository repository;

    public JpaCvMatchAnalysisRepository(SpringDataCvMatchAnalysisRepository repository){
        this.repository = repository;
    }

    @Override
    public CvMatchAnalysis save(CvMatchAnalysis cvMatchAnalysis) {
        return repository.save(cvMatchAnalysis);
    }

    @Override
    public Optional<CvMatchAnalysis> findById(UUID id) {
        return repository.findById(id);
    }
}
