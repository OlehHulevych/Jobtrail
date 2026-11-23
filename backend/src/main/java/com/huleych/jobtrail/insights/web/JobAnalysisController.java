package com.huleych.jobtrail.insights.web;

import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.insights.application.JobAnalysisChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class JobAnalysisController {
    private final JobAnalysisChatService jobAnalysisChatService;

    public JobAnalysisController(JobAnalysisChatService jobAnalysisChatService) {
        this.jobAnalysisChatService = jobAnalysisChatService;
    }

    @PostMapping("/{id}/analysis")
    public JobAnalysisResponse analyse(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return jobAnalysisChatService.execute(user.getId(), id);
    }
}
