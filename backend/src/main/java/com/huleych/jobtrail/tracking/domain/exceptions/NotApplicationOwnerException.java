package com.huleych.jobtrail.tracking.domain.exceptions;

import java.util.UUID;

public class NotApplicationOwnerException extends RuntimeException {
    public NotApplicationOwnerException(UUID requestUserId, UUID applicationId) {
        super("Owner "+requestUserId+" access is forbidden to get application "+applicationId);
    }
}
