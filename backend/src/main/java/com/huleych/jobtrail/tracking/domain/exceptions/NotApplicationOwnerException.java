package com.huleych.jobtrail.tracking.domain.exceptions;

public class NotApplicationOwnerException extends RuntimeException {
    public NotApplicationOwnerException(Long requestUserId, Long applicationId) {
        super("Owner "+requestUserId+" access is forbidden to get application "+applicationId);
    }
}
