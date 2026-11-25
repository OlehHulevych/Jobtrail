package com.huleych.jobtrail.identity.application.result;


import com.huleych.jobtrail.identity.domain.enums.ExtractionStatus;

import java.util.UUID;

public record UploadResult(UUID id, String originalName, String contentType, ExtractionStatus extractionStatus) {
}
