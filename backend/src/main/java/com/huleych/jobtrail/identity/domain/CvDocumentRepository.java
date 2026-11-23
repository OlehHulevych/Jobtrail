package com.huleych.jobtrail.identity.domain;

import java.util.Optional;
import java.util.UUID;

public interface CvDocumentRepository {

    CvDocument save(CvDocument cvDocument);
    Optional<CvDocument> findById(UUID id);

}
