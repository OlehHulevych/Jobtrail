package com.huleych.jobtrail.insights.infrastructure.repository;

import com.huleych.jobtrail.insights.domain.entity.CvMatchAnalysis;

import java.util.Optional;
import java.util.UUID;

public interface CvMatchAnalysisRepository {

    CvMatchAnalysis save(CvMatchAnalysis cvMatchAnalysis);
    Optional<CvMatchAnalysis> findById(UUID id);

}
