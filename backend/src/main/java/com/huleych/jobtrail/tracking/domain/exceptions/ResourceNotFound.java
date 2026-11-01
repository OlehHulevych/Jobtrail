package com.huleych.jobtrail.tracking.domain.exceptions;

public class ResourceNotFound extends RuntimeException {
    public ResourceNotFound(String type, Long id) {
        super(type+" was not found by id: "+id);
    }
}
