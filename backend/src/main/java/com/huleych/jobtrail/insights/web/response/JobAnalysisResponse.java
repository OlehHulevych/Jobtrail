package com.huleych.jobtrail.insights.web.response;

import com.huleych.jobtrail.insights.domain.enums.Seniority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record JobAnalysisResponse(
        UUID id,
        UUID applicationId,
        String summary,
        Seniority seniority,
        List<String> requiredSkills,
        List<String> niceToHaveSkills,
        List<String> responsibilities,
        List<String> redFlags,
        LocalDateTime createdAt) {
}
