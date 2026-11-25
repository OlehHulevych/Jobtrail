package com.huleych.jobtrail.identity.application.commands;

import java.io.InputStream;
import java.util.UUID;

public record UploadCvCommand(UUID userId, String originalName, String contentType, long size, InputStream content) {
}
