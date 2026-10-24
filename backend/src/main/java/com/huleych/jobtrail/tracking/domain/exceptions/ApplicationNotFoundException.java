package com.huleych.jobtrail.tracking.domain.exceptions;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(Long id) {
        super("Cannot find application to this id: "+id);
    }
}
