package com.huleych.jobtrail.identity.domain.exceptions;

public class GithubAuthenticationException extends RuntimeException {
    public GithubAuthenticationException(String message) {
        super(message);
    }
}
