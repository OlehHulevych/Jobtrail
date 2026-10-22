package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.exceptions.InvalidStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class JobApplicationTest {
    private String company = "Acme";
    private String position = "Java Developer";
    JobApplication jobApplication;



    @BeforeEach
    void init() {
        jobApplication = new JobApplication(1L, "Acme", "Java Developer", "https://acme.example/jobs/1",  "Build backend services with Spring Boot");
    }
    @Test
    public void shouldStartInWithListWhenCreated(){
        assertEquals(ApplicationStatus.WISHLIST, jobApplication.getStatus());
    }
    @Test
    public void shouldStoreOwnerCompanyAndPositionWhenCreated(){

        assertEquals(1L,jobApplication.getOwnerId());
        assertEquals(company,jobApplication.getCompany());
        assertEquals(position,jobApplication.getPosition());

    }
    @Test
    public void shouldRejectNullOwner(){
        assertThrows(IllegalArgumentException.class, ()->new JobApplication(null ,"Acme", "Java Developer", "https://acme.example/jobs/1",  "Build backend services with Spring Boot"));
    }
    @Test
    public void shouldRejectNullCompany(){
        assertThrows(IllegalArgumentException.class, ()->new JobApplication(1L ,null, "Java Developer", "https://acme.example/jobs/1",  "Build backend services with Spring Boot"));
    }
    @Test
    public void shouldRejectBlankCompany(){
        assertThrows(IllegalArgumentException.class, ()->new JobApplication(1L ," ", "Java Developer", "https://acme.example/jobs/1",  "Build backend services with Spring Boot"));
    }
    @Test
    public void shouldRejectBlankPosition(){
        assertThrows(IllegalArgumentException.class, ()->new JobApplication(1L ,"Acme", " ", "https://acme.example/jobs/1",  "Build backend services with Spring Boot"));
    }
    @Test
    public void shouldAllowMissingUrlAndDescription(){
        JobApplication jobApplication = new JobApplication(1L, "Acme", "Java Developer", null,null);
        assertNull(jobApplication.getUrl());
        assertNull(jobApplication.getJobDescription());
    }

    @Test
    public void shouldSetStatusAndAppliedAtWhenApplying(){
        jobApplication.apply(LocalDate.now());
        assertEquals(ApplicationStatus.APPLIED, jobApplication.getStatus());
        assertEquals(LocalDate.now(),jobApplication.getAppliedAt());
    }
    @Test
    public void shouldRejectNullApplyDate(){
        assertThrows(IllegalArgumentException.class, ()->jobApplication.apply(null));
    }
    @Test
    public void shouldRejectApplyingTwice(){
        assertThrows(InvalidStatusTransitionException.class, ()->{
            jobApplication.apply(LocalDate.now());
            jobApplication.apply(LocalDate.now());
        });
    }

}
