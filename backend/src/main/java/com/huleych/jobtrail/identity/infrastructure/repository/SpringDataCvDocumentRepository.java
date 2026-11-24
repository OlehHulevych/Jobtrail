package com.huleych.jobtrail.identity.infrastructure.repository;

import com.huleych.jobtrail.identity.domain.entity.CvDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCvDocumentRepository extends JpaRepository<CvDocument, UUID> {
}
