package com.huleych.jobtrail.tracking.domain.exceptions;

import java.util.UUID;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(UUID id) {
        super("Cannot find application to this id: "+id);
    }
}
