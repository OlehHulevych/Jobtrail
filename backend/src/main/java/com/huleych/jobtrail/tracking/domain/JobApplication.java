package com.huleych.jobtrail.tracking.domain;

import com.huleych.jobtrail.tracking.domain.exceptions.InvalidStatusTransitionException;
import com.huleych.jobtrail.tracking.domain.exceptions.ResourceNotFoundException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "job_applications")
public class JobApplication extends BaseEntity {
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    @Column(nullable = false)
    private String company;
    @Column(nullable = false)
    private String position;
    private String url;
    @Column(columnDefinition = "text")
    private String jobDescription;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    @OneToMany(mappedBy = "jobApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC ")
    private List<Note> notes = new ArrayList<Note>();
    private LocalDate appliedAt;

    protected JobApplication(){

    }

    public JobApplication(UUID ownerId, String company, String position, String url, String jobDescription){
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

    public UUID getOwnerId() {
        return ownerId;
    }


    public void addNote(String value){
        if(status==ApplicationStatus.WITHDRAWN||status==ApplicationStatus.REJECTED){
            throw new IllegalStateException("Invalid status of application");
        }
        Note note = new Note(value, this);
        notes.add(note);
    }

    public void deleteNote(UUID id){
         if(status== ApplicationStatus.REJECTED || status==ApplicationStatus.WITHDRAWN) throw new IllegalStateException("Invalid staus to perform action");
         if(id==null) throw new IllegalArgumentException("note's id is required");
         Note note = notes.stream().filter(n-> Objects.equals(n.getId(), id)).findFirst().orElse(null);
         if(note==null) throw new ResourceNotFoundException(Note.class.getName(), id);
         notes.remove(note);
    }

    public List<Note> getNotes(){
        return List.copyOf(notes);
    }

}