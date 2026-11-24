package com.huleych.jobtrail.insights.domain.entity;

import com.huleych.jobtrail.insights.domain.enums.Seniority;
import com.huleych.jobtrail.tracking.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "job_analyses")
public class JobAnalysis extends BaseEntity {
    @Column(name = "application_id", nullable = false, unique = true)
    private UUID applicationId;
    @Column(columnDefinition = "text")
    private String summary;
    @Enumerated(EnumType.STRING)
    private Seniority seniority;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> requiredSkills;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> niceToHaveSkills;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> responsibilities;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> redFlags;

    protected JobAnalysis() {

    }

    public JobAnalysis(UUID applicationId, String summary, Seniority seniority, List<String> requiredSkills,
                       List<String> niceToHaveSkills, List<String> responsibilities, List<String> redFlags) {
        if (applicationId == null) throw new IllegalArgumentException("Application is required");
        this.applicationId = applicationId;
        assign(summary, seniority, requiredSkills, niceToHaveSkills, responsibilities, redFlags);
    }

    public void replaceWith(String summary, Seniority seniority, List<String> requiredSkills,
                            List<String> niceToHaveSkills, List<String> responsibilities, List<String> redFlags) {
        assign(summary, seniority, requiredSkills, niceToHaveSkills, responsibilities, redFlags);
    }

    private void assign(String summary, Seniority seniority, List<String> requiredSkills,
                        List<String> niceToHaveSkills, List<String> responsibilities, List<String> redFlags) {
        this.summary = summary;
        this.seniority = seniority;
        this.requiredSkills = requiredSkills == null ? List.of() : List.copyOf(requiredSkills);
        this.niceToHaveSkills = niceToHaveSkills == null ? List.of() : List.copyOf(niceToHaveSkills);
        this.responsibilities = responsibilities == null ? List.of() : List.copyOf(responsibilities);
        this.redFlags = redFlags == null ? List.of() : List.copyOf(redFlags);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getSummary() {
        return summary;
    }

    public Seniority getSeniority() {
        return seniority;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public List<String> getNiceToHaveSkills() {
        return niceToHaveSkills;
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }

    public List<String> getRedFlags() {
        return redFlags;
    }
}
