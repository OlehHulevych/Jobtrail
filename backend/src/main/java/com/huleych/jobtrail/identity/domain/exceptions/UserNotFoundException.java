package com.huleych.jobtrail.identity.domain.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User was not found by id: " + id);
    }
}
