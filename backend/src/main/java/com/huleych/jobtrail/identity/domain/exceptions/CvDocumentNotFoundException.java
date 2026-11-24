package com.huleych.jobtrail.identity.domain.exceptions;

import java.util.UUID;

public class CvDocumentNotFoundException extends RuntimeException {
    public CvDocumentNotFoundException(UUID id) {
        super("Cv document was not found by id: " + id);
    }
}
