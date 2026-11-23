package com.huleych.jobtrail.identity.infrastructure;

import com.huleych.jobtrail.identity.domain.CvDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCvDocumentRepository extends JpaRepository<CvDocument, UUID> {
}
