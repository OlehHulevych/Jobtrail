package com.huleych.jobtrail.identity.infrastructure.storage;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public interface FileStorage {
    void store(String key, InputStream content, long size, String contentType);
    void delete(String key);
    URL downloadUrl(String key, Duration validFor);
}
