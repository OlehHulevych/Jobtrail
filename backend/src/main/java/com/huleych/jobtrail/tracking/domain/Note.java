package com.huleych.jobtrail.tracking.domain;

import jakarta.persistence.*;


@Entity
@Table(name = "notes")
public class Note extends BaseEntity {
    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_application_id", nullable = false )
    private JobApplication jobApplication;

    Note(String value, JobApplication jobApplication){
      if(jobApplication==null) throw new IllegalArgumentException("job application is required");
      if(value==null || value.isBlank()) throw new IllegalArgumentException("Value is required for note");
      this.value = value.trim();
      this.jobApplication = jobApplication;
    }

    protected  Note(){

    }
    public String getValue(){
        return value;
    }



}
