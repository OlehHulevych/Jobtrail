package com.huleych.jobtrail.insights.domain.exceptions;

import java.util.UUID;

public class JobAnalysisFailedException extends RuntimeException {
    public JobAnalysisFailedException(UUID applicationId) {
        super("AI returned no analysis for application: " + applicationId);
    }
}
