package com.huleych.jobtrail.identity.domain.exceptions;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException() {
        super("Refresh token was not found");
    }
}
