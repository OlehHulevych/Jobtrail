package com.huleych.jobtrail.insights.domain.exceptions;

import java.util.UUID;

public class CvAnalysisFailedException extends RuntimeException {
    public CvAnalysisFailedException(UUID cvId) {
        super("AI returned no analysis for cv document: " + cvId);
    }
}
