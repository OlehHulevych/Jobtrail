package com.huleych.jobtrail.tracking.domain;

import com.huleych.jobtrail.tracking.domain.exceptions.InvalidStatusTransitionException;
import jakarta.persistence.*;


import java.time.LocalDate;


@Entity
@Table(name = "job_applications")
public class JobApplication extends BaseEntity {



    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(nullable = false)
    private String company;
    @Column(nullable = false)
    private String position;
    private String url;
    @Column(columnDefinition = "text")
    private String jobDescription;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    private LocalDate appliedAt;

    protected JobApplication(){

    }

    public JobApplication(Long ownerId, String company, String position, String url, String jobDescription){
        if(ownerId==null ) throw new IllegalArgumentException("Owner is required");
        if(company == null || company.isBlank()) throw new IllegalArgumentException("Company is required");
        if(position == null || position.isBlank()) throw new IllegalArgumentException("Position is required");
        this.ownerId = ownerId;
        this.company = company;
        this.position = position;
        status = ApplicationStatus.WISHLIST;
        this.url = url;
        this.jobDescription = jobDescription;
    }

    public void apply(LocalDate today){
        if(today==null) throw new IllegalArgumentException("Time of applying is required");
        changeStatus(ApplicationStatus.APPLIED);
        appliedAt = today;
    }

    public void editDetails(String company, String position, String url, String jobDescription){
        if(company==null || company.isBlank()) throw new IllegalArgumentException("company is required");
        if(position==null || position.isBlank()) throw new IllegalArgumentException("position is required");
        this.position = position;
        this.company = company;
        if(url!=null && !url.isBlank()) this.url = url;
        if(jobDescription!=null && !jobDescription.isBlank())this.jobDescription = jobDescription;
    }

    public void moveTo(ApplicationStatus status){
        if(status==ApplicationStatus.APPLIED) throw new IllegalArgumentException("Cannot change status to Applied");
        changeStatus(status);
    }

    private void changeStatus(ApplicationStatus status){
        if(status==null) throw new IllegalArgumentException("Status is required");
        if(!this.status.canTransferTo(status)) throw new InvalidStatusTransitionException(this.status, status);
        this.status = status;
    }

    public String getCompany() {
        return company;
    }

    public String getPosition() {
        return position;
    }

    public String getUrl() {
        return url;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDate getAppliedAt() {
        return appliedAt;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}