package com.huleych.jobtrail.identity.infrastructure.config;

import com.huleych.jobtrail.identity.infrastructure.properties.S3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {
    @Bean
    S3Client s3Client(S3Properties p){
        return S3Client.builder().endpointOverride(p.endpoint())
                .region(Region.of(p.region()))
                .credentialsProvider(credentials(p))
                .forcePathStyle(p.pathStyle())
                .build();

    }
    @Bean
    S3Presigner s3Presigner(S3Properties p){
        return S3Presigner.builder()
                .endpointOverride(p.endpoint())
                .region(Region.of(p.region()))
                .credentialsProvider(credentials(p))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(p.pathStyle()).build())
                .build();
    }

    private StaticCredentialsProvider credentials(S3Properties p){
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(p.accessKey(),p.secretKey()));
    }
}
