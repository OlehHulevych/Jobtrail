package com.huleych.jobtrail.insights.application;

import com.huleych.jobtrail.insights.domain.Seniority;

import java.util.List;

public record JobAnalysisResult(
        String summary,
        Seniority seniority,
        List<String> requiredSkills,
        List<String> niceToHaveSkills,
        List<String> responsibilities,
        List<String> redFlags) {
}
