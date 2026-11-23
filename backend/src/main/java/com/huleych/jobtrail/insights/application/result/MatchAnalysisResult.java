package com.huleych.jobtrail.insights.application.result;

import java.util.List;

public record MatchAnalysisResult(
        int matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> suggestions
) {
}
