package com.huleych.jobtrail.insights.application.service;

import com.huleych.jobtrail.identity.domain.entity.CvDocument;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.domain.enums.ExtractionStatus;
import com.huleych.jobtrail.identity.domain.exceptions.CvDocumentNotFoundException;
import com.huleych.jobtrail.identity.domain.exceptions.NotCvDocumentOwnerException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundException;
import com.huleych.jobtrail.identity.infrastructure.repository.CvDocumentRepository;
import com.huleych.jobtrail.identity.infrastructure.repository.UserRepository;
import com.huleych.jobtrail.insights.application.commands.AnalyzeMatchCommand;
import com.huleych.jobtrail.insights.application.result.MatchAnalysisResult;
import com.huleych.jobtrail.insights.domain.entity.CvMatchAnalysis;
import com.huleych.jobtrail.insights.infrastructure.repository.CvMatchAnalysisRepository;
import com.huleych.jobtrail.insights.domain.exceptions.CvAnalysisFailedException;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import com.huleych.jobtrail.tracking.domain.exceptions.ApplicationNotFoundException;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import org.springframework.core.io.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class CvAnalysisChatService {
    private final ChatClient client;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CvDocumentRepository cvDocumentRepository;
    private final CvMatchAnalysisRepository cvMatchAnalysisRepository;
    private final Resource analysisPrompt;
    private final Resource userPrompt;

    public CvAnalysisChatService(UserRepository userRepository, JobApplicationRepository jobApplicationRepository,
                                 CvDocumentRepository cvDocumentRepository, @Value("classpath:/prompts/cv-analysis.st") Resource analysisPrompt, ChatClient.Builder builder,
                                 @Value("classpath:/prompts/cv-user-analysis.st") Resource userPrompt, CvMatchAnalysisRepository cvMatchAnalysisRepository) {
        this.userRepository = userRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.cvDocumentRepository = cvDocumentRepository;
        this.client = builder.build();
        this.analysisPrompt = analysisPrompt;
        this.userPrompt = userPrompt;
        this.cvMatchAnalysisRepository = cvMatchAnalysisRepository;
    }

    public MatchAnalysisResult execute(AnalyzeMatchCommand command) {
        if (command == null) throw new IllegalArgumentException("Command request is required");
        if (command.applicationId() == null) throw new IllegalArgumentException("job application id is required");
        if (command.cvId() == null) throw new IllegalArgumentException("Cv id is required");
        if (command.userId() == null) throw new IllegalArgumentException("Id of User is required");

        JobApplication jobApplication = jobApplicationRepository.findById(command.applicationId()).orElse(null);
        if (jobApplication == null) throw new ApplicationNotFoundException(command.applicationId());
        CvDocument cvDocument = cvDocumentRepository.findById(command.cvId()).orElse(null);
        if (cvDocument == null) throw new CvDocumentNotFoundException(command.cvId());
        User user = userRepository.findById(command.userId()).orElse(null);
        if (user == null) throw new UserNotFoundException(command.userId());
        if(!cvDocument.getUser().getId().equals(user.getId())) throw new NotCvDocumentOwnerException(user.getId(), cvDocument.getId());
        if(!jobApplication.getOwnerId().equals(user.getId())) throw new  NotApplicationOwnerException(user.getId(), jobApplication.getId());
        if(cvDocument.getExtractionStatus()!= ExtractionStatus.DONE) throw new IllegalStateException("Text is not extracted");
        MatchAnalysisResult result = client.prompt().system(analysisPrompt).user(u -> u.text(userPrompt).param("jobDescription", jobApplication.getJobDescription())
                .param("cvText", cvDocument.getExtractedText())).call().entity(MatchAnalysisResult.class);
        if(result==null) throw new CvAnalysisFailedException(cvDocument.getId());
        if(result.matchScore()<0 || result.matchScore()>100) throw new CvAnalysisFailedException(cvDocument.getId());
        String suggestions = result.suggestions()==null ? null : String.join(", ",result.suggestions());
        CvMatchAnalysis cvMatchAnalysis = new CvMatchAnalysis(jobApplication,cvDocument,user, result.matchScore(), suggestions,result.matchedSkills(),result.missingSkills());
        cvMatchAnalysisRepository.save(cvMatchAnalysis);
        return result;


    }
}