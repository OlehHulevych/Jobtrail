package com.huleych.jobtrail.identity.infrastructure.repository;

import com.huleych.jobtrail.identity.domain.entity.CvDocument;

import java.util.Optional;
import java.util.UUID;

public interface CvDocumentRepository {

    CvDocument save(CvDocument cvDocument);
    Optional<CvDocument> findById(UUID id);

}
