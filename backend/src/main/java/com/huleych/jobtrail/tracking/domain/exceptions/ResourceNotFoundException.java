package com.huleych.jobtrail.tracking.domain.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String type, Long id) {
        super(type+" was not found by id: "+id);
    }
}
