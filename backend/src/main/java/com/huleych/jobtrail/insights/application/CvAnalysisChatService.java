package com.huleych.jobtrail.insights.application;

import com.huleych.jobtrail.identity.domain.UserRepository;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import org.springframework.ai.chat.client.ChatClient;

public class CvAnalysisChatService {
    private final ChatClient client;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final
}
