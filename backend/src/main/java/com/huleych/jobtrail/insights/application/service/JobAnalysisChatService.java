package com.huleych.jobtrail.insights.application.service;

import com.huleych.jobtrail.insights.application.result.JobAnalysisResult;
import com.huleych.jobtrail.insights.domain.entity.JobAnalysis;
import com.huleych.jobtrail.insights.infrastructure.repository.JobAnalysisRepository;
import com.huleych.jobtrail.insights.domain.exceptions.JobAnalysisFailedException;
import com.huleych.jobtrail.insights.web.response.JobAnalysisResponse;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobAnalysisChatService {
    private final ChatClient builder;
    private final JobApplicationRepository jobApplicationRepository;
    private final Resource analysisPrompt;
    private final JobAnalysisRepository jobAnalysisRepository;
    public JobAnalysisChatService(JobApplicationRepository jobApplicationRepository, @Value("classpath:/prompts/job-analysis.st") Resource analysisPrompt, ChatClient.Builder builder, JobAnalysisRepository jobAnalysisRepository){
        this.builder = builder.build();
        this.jobApplicationRepository = jobApplicationRepository;
        this.analysisPrompt = analysisPrompt;
        this.jobAnalysisRepository = jobAnalysisRepository;
    }

    public JobAnalysisResponse execute(UUID userId, UUID applicationId){
        if(userId==null) throw new IllegalArgumentException("User id is required");
        if(applicationId==null) throw new IllegalArgumentException("Application id is required");

        JobApplication application = jobApplicationRepository.findById(applicationId).orElse(null);
        if(application==null) throw new ApplicationNotFoundException(applicationId);
        if(!application.getOwnerId().equals(userId)) throw new NotApplicationOwnerException(userId, applicationId);

        JobAnalysisResult result = builder.prompt().user(u->u.text(analysisPrompt).param("jobDescription",application.getJobDescription())).call().entity(JobAnalysisResult.class);
        if(result==null) throw new JobAnalysisFailedException(applicationId);

        JobAnalysis jobAnalysis = jobAnalysisRepository.findByApplicationId(applicationId).orElse(null);
        if(jobAnalysis==null) {
            jobAnalysis = new JobAnalysis(application.getId(),result.summary(),result.seniority(),result.requiredSkills(),result.niceToHaveSkills(),result.responsibilities(),result.redFlags());
        } else {
            jobAnalysis.replaceWith(result.summary(),result.seniority(),result.requiredSkills(),result.niceToHaveSkills(),result.responsibilities(),result.redFlags());
        }
        JobAnalysis saved = jobAnalysisRepository.save(jobAnalysis);
        return new JobAnalysisResponse(saved.getId(),saved.getApplicationId(),saved.getSummary(),
                saved.getSeniority(),saved.getRequiredSkills()
                ,saved.getNiceToHaveSkills(),
                saved.getResponsibilities(),
                saved.getRedFlags(),saved.getCreatedAt());

    }
}
