package com.huleych.jobtrail.tracking.domain;

import jakarta.persistence.*;

import java.time.Clock;
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
    private String url = null;
    @Column(columnDefinition = "text")
    private String jobDescription = null;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    private LocalDate appliedAt = null;

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
        if(url!=null) this.url = url;
        if(jobDescription!=null) this.jobDescription = jobDescription;


    }



    public void apply(LocalDate today){
        if(today==null) throw new IllegalArgumentException("Clock is required");
        if(status == ApplicationStatus.WISHLIST) {
            setStatus(ApplicationStatus.APPLIED);
            appliedAt = today;
        }
        else{
            throw new IllegalArgumentException("Can't apply application.Cannot change status of application");
        }
    }

    public void scheduleInterview(){
        if(status==ApplicationStatus.INTERVIEW || status == ApplicationStatus.APPLIED) {
            setStatus(ApplicationStatus.INTERVIEW);
        }
        else{
            throw new IllegalArgumentException("Can't schedule interview");
        }
    }

    public void offer(){
        if(status==ApplicationStatus.INTERVIEW || status == ApplicationStatus.APPLIED) {
            setStatus(ApplicationStatus.OFFER);
        }
        else{
            throw new IllegalArgumentException("Can't give offer");
        }
    }
    public void reject(){
        if(status==ApplicationStatus.INTERVIEW || status == ApplicationStatus.APPLIED) {
            setStatus(ApplicationStatus.REJECTED);
        }
        else{
            throw new IllegalArgumentException("Can't reject application");
        }
    }
    public void withdraw(){
        if(status==ApplicationStatus.INTERVIEW || status == ApplicationStatus.APPLIED || status == ApplicationStatus.WISHLIST) {
            setStatus(ApplicationStatus.WITHDRAWN);
        }
        else{
            throw new IllegalArgumentException("Can't withdraw application");
        }
    }

    public void editDetails(String company, String position, String url, String jobDescription){
       this.company = company==null || company.isBlank()?this.company:company;
       this.position = position==null || position.isBlank()?this.position:position;
       this.url = url==null || url.isBlank()?this.url:url;
       this.jobDescription = jobDescription==null || jobDescription.isBlank()?this.jobDescription:jobDescription;

    }

    private void setStatus(ApplicationStatus status){
        if(status!=null){
            this.status = status;
        }
        else {
            throw new IllegalArgumentException("Status is required!");
        }

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