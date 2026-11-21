package com.huleych.jobtrail.identity.infrastructure;

public interface GithubClient {
    GithubProfile fetchProfile(String code);
}
