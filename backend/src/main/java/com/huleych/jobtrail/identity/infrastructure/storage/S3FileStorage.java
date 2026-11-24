package com.huleych.jobtrail.identity.infrastructure.storage;

import com.huleych.jobtrail.identity.infrastructure.properties.S3Properties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

@Component
public class S3FileStorage implements FileStorage {
    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    S3FileStorage(S3Client s3, S3Presigner presigner, S3Properties props){
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = props.bucket();
    }
    @Override
    public void store(String key, InputStream content, long size, String contentType) {
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
                .contentType(contentType).build(),
                RequestBody.fromInputStream(content,size));

    }

    @Override
    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());

    }

    @Override
    public URL downloadUrl(String key, Duration validFor) {
        var get = GetObjectRequest.builder().bucket(bucket).key(key).build();
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(validFor).getObjectRequest(get).build()).url();
    }
}
