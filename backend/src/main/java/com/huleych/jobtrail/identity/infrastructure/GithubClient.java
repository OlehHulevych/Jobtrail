package com.huleych.jobtrail.identity.infrastructure;

public interface GithubClient {
    String fetchVerifiedEmail(String code);
}
