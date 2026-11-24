package com.huleych.jobtrail.identity.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix="storage.s3")
public record S3Properties(URI endpoint, String region, String accessKey, String secretKey, String bucket, boolean pathStyle) {

}
