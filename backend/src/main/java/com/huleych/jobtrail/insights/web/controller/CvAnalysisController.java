package com.huleych.jobtrail.insights.web.controller;

import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.insights.application.commands.AnalyzeMatchCommand;
import com.huleych.jobtrail.insights.application.result.MatchAnalysisResult;
import com.huleych.jobtrail.insights.application.service.CvAnalysisChatService;
import com.huleych.jobtrail.insights.web.request.AnalyzeMatchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cv/analysis")
public class CvAnalysisController {
    private final CvAnalysisChatService cvAnalysisChatService;

    public CvAnalysisController(CvAnalysisChatService cvAnalysisChatService){
        this.cvAnalysisChatService = cvAnalysisChatService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchAnalysisResult analyze(@AuthenticationPrincipal User user, @RequestBody AnalyzeMatchRequest request){
        return cvAnalysisChatService.execute(new AnalyzeMatchCommand(user.getId(), request.applicationId(), request.cvId()));
    }

}
