package com.huleych.jobtrail.insights.application.commands;

import java.util.UUID;

public record AnalyzeMatchCommand(UUID userId, UUID applicationId, UUID cvId) {
}
