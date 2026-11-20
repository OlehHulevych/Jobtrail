package com.huleych.jobtrail.insights.web;

import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.insights.application.ChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class JobAnalysisController {
    private final ChatService chatService;

    public JobAnalysisController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{id}/analysis")
    public JobAnalysisResponse analyse(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return chatService.execute(user.getId(), id);
    }
}
