package com.huleych.jobtrail.identity.infrastructure.client;

public interface GithubClient {
    GithubProfile fetchProfile(String code);
}
