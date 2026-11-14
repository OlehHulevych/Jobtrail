package com.huleych.jobtrail.tracking.domain.exceptions;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String type, UUID id) {
        super(type+" was not found by id: "+id);
    }
}
