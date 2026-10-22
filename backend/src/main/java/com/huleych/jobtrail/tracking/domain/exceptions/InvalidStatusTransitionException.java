package com.huleych.jobtrail.tracking.domain.exceptions;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(ApplicationStatus currentStatus, ApplicationStatus targetStatus) {
        super(currentStatus.name()+" can't change to "+targetStatus.name());
    }
}
