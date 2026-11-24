package com.huleych.jobtrail.insights.domain;

import com.huleych.jobtrail.identity.domain.entity.CvDocument;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.domain.exceptions.NotCvDocumentOwnerException;
import com.huleych.jobtrail.tracking.domain.BaseEntity;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.exceptions.NotApplicationOwnerException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.util.List;

@Entity
@Getter
@Table(name = "cv_match_analyses")
public class CvMatchAnalysis extends BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication jobApplication;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cv_id", nullable = false)
    private CvDocument cvDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = true)
    @Min(0)
    @Max(100)
    private Integer matchScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> matchedSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> missingSkills;

    @Column(nullable = true, columnDefinition = "text")
    private String suggestions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    protected CvMatchAnalysis(){

    }

    public CvMatchAnalysis(JobApplication jobApplication, CvDocument cvDocument, User user, int matchScore,String suggestions, List<String> matchedSkills, List<String> missingSkills){
        if(jobApplication==null) throw new IllegalArgumentException("Job application is required");
        if(cvDocument==null) throw new IllegalArgumentException("Cv document is required");
        if(user==null) throw new IllegalArgumentException("user is required");
        if(!user.getId().equals(jobApplication.getOwnerId())) throw new NotApplicationOwnerException(user.getId(), jobApplication.getId());
        if(!user.getId().equals(cvDocument.getUser().getId())) throw new NotCvDocumentOwnerException(user.getId(), cvDocument.getId());
        if(matchScore>100 || matchScore<0) throw new IllegalArgumentException("match score should be in range between 0 and 100");
        this.jobApplication = jobApplication;
        this.cvDocument = cvDocument;
        this.user = user;
        this.matchScore = matchScore;
        this.suggestions = suggestions;
        this.matchedSkills = matchedSkills == null ? List.of() : List.copyOf(matchedSkills);
        this.missingSkills = missingSkills == null ? List.of() : List.copyOf(missingSkills);
        this.status = AnalysisStatus.PENDING;
    }







}
