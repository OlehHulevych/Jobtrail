package com.huleych.jobtrail.identity.domain;

import com.huleych.jobtrail.tracking.domain.BaseEntity;
import jakarta.persistence.*;

import lombok.Getter;



@Entity
@Table(name = "cv_documents")
@Getter
public class CvDocument extends BaseEntity {
    @Column(nullable = false)
    private String objectKey;
    @Column(nullable = false)
    private String originalName;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = true, columnDefinition = "TEXT")
    private String extractedText;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExtractionStatus extractionStatus;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected CvDocument(){

    }

    public CvDocument(User user, String objectKey, String originalName, String contentType){
        if (user == null) throw new IllegalArgumentException("User is required");
        if (objectKey == null || objectKey.isBlank()) throw new IllegalArgumentException("Object key is required");
        if (originalName == null || originalName.isBlank()) throw new IllegalArgumentException("Original name is required");
        if (contentType == null || contentType.isBlank()) throw new IllegalArgumentException("Content type is required");
        this.user = user;
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.contentType = contentType;
        this.extractionStatus = ExtractionStatus.PENDING;
    }








}
