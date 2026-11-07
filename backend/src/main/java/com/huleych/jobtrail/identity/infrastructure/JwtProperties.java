package com.huleych.jobtrail.identity.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration accessTtl, Duration refreshTtl) {
}
