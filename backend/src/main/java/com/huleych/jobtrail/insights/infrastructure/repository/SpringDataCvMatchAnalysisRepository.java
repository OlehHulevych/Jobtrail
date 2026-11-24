package com.huleych.jobtrail.insights.infrastructure.repository;

import com.huleych.jobtrail.insights.domain.entity.CvMatchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCvMatchAnalysisRepository extends JpaRepository<CvMatchAnalysis, UUID> {
}
