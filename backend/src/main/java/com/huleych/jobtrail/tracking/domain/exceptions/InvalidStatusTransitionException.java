package com.huleych.jobtrail.tracking.domain.exceptions;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(ApplicationStatus currentStatus, ApplicationStatus targetStatus) {
        super(currentStatus.name()+" can't change to "+targetStatus.name());
    }
}
