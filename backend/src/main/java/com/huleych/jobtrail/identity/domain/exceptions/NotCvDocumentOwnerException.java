package com.huleych.jobtrail.identity.domain.exceptions;

import java.util.UUID;

public class NotCvDocumentOwnerException extends RuntimeException {
    public NotCvDocumentOwnerException(UUID requestUserId, UUID cvDocumentId) {
        super("User " + requestUserId + " is not the owner of cv document " + cvDocumentId);
    }
}
