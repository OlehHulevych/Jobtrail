package com.huleych.jobtrail.insights.web.request;

import java.util.UUID;

public record AnalyzeMatchRequest(UUID applicationId, UUID cvId) {
}
