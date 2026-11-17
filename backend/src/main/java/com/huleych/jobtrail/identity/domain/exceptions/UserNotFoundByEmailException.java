package com.huleych.jobtrail.identity.domain.exceptions;

public class UserNotFoundByEmailException extends RuntimeException {
    public UserNotFoundByEmailException(String email) {
        super("User was not found by email: " + email);
    }
}
