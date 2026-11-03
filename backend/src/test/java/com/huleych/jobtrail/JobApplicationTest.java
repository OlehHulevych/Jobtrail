package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.exceptions.InvalidStatusTransitionException;
import com.huleych.jobtrail.tracking.domain.exceptions.ResourceNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class JobApplicationTest {
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
        String company = "Acme";
        assertEquals(company,jobApplication.getCompany());
        String position = "Java Developer";
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

    @Test
    public void shouldMoveFromAppliedToInterview(){
        jobApplication.apply(LocalDate.now());
        jobApplication.moveTo(ApplicationStatus.INTERVIEW);
        assertEquals(ApplicationStatus.INTERVIEW, jobApplication.getStatus());
    }
    @Test
    public void shouldAllowInterviewToInterview (){
        jobApplication.apply(LocalDate.now());
        jobApplication.moveTo(ApplicationStatus.INTERVIEW);
        jobApplication.moveTo(ApplicationStatus.INTERVIEW);
        assertEquals(ApplicationStatus.INTERVIEW, jobApplication.getStatus());
    }
    @Test
    public void shouldMoveFromInterviewToOffer(){
        jobApplication.apply(LocalDate.now());
        jobApplication.moveTo(ApplicationStatus.INTERVIEW);
        jobApplication.moveTo(ApplicationStatus.OFFER);
        assertEquals(ApplicationStatus.OFFER, jobApplication.getStatus());
    }
    @Test
    public void shouldRejectMoveToApplied (){
        assertThrows(InvalidStatusTransitionException.class, ()->{
            jobApplication.apply(LocalDate.now());
            jobApplication.moveTo(ApplicationStatus.INTERVIEW);
            jobApplication.apply(LocalDate.now());
        });

    }
    @Test
    public void shouldRejectNullStatus(){
        assertThrows(IllegalArgumentException.class, ()->{
            jobApplication.apply(LocalDate.now());
            jobApplication.moveTo(null);
        });

    }
    @Test
    public void shouldRejectTransitionFromWishlistToInterview(){
        assertThrows(InvalidStatusTransitionException.class, ()->{
            jobApplication.moveTo(ApplicationStatus.INTERVIEW);
        });
    }
    @Test
    public void shouldRejectTransitionFromRejectedToInterview (){
        assertThrows(InvalidStatusTransitionException.class, ()->{
            jobApplication.apply(LocalDate.now());
            jobApplication.moveTo(ApplicationStatus.REJECTED);
            jobApplication.moveTo(ApplicationStatus.INTERVIEW);
        });
    }
    @Test
    public void shouldRejectAnyTransitionFromOffer(){
        assertThrows(InvalidStatusTransitionException.class, ()->{
            jobApplication.apply(LocalDate.now());
            jobApplication.moveTo(ApplicationStatus.OFFER);
            jobApplication.moveTo(ApplicationStatus.INTERVIEW);
        });
    }
    @Test
    public void shouldAllowWithdrawFromWishlist(){
        jobApplication.moveTo(ApplicationStatus.WITHDRAWN);
        assertEquals(ApplicationStatus.WITHDRAWN, jobApplication.getStatus());
    }

    @Test
    public void shouldChangeAllDetailsWhenEditing(){
        jobApplication.editDetails("Globex", "Backend Engineer", "https://globex.example/careers/42", "Design REST APIs and own the database layer"  );
        assertEquals("Globex",jobApplication.getCompany());
        assertEquals("Backend Engineer",jobApplication.getPosition());
        assertEquals("https://globex.example/careers/42",jobApplication.getUrl());
        assertEquals("Design REST APIs and own the database layer", jobApplication.getJobDescription());
    }

    @Test
    public void shouldRejectBlankCompanyWhenEditing (){
        assertThrows(IllegalArgumentException.class, ()->{
            jobApplication.editDetails("", "Backend Engineer", "https://globex.example/careers/42", "Design REST APIs and own the database layer"  );

        });
    }
    @Test
    public void shouldRejectBlankPositionWhenEditing(){
        assertThrows(IllegalArgumentException.class, ()->{
            jobApplication.editDetails("Globex", "", "https://globex.example/careers/42", "Design REST APIs and own the database layer"  );

        });
    }
    @Test
    public void shouldKeepOldUrlWhenNewUrlIsNull (){
        jobApplication.editDetails("Globex", "Backend Engineer", null,"Design REST APIs and own the database layer"  );
        assertEquals("https://acme.example/jobs/1", jobApplication.getUrl());
    }
    @Test
    public void shouldKeepOldDescriptionWhenNewOneIsBlank(){
        jobApplication.editDetails("Globex", "Backend Engineer", "https://globex.example/careers/42",null  );
        assertEquals("Build backend services with Spring Boot", jobApplication.getJobDescription());
    }

    @Nested
    class AddNote{
        @Test
        public void  shouldAddNoteToNewApplication(){
            jobApplication.addNote("Prepare to HR interview");
            assertEquals(1, jobApplication.getNotes().size());
        }

        @Test
        public void shouldTrimNoteText(){
            jobApplication.addNote("   Prepare to HR interview   ");
            assertEquals("Prepare to HR interview", jobApplication.getNotes().getFirst().getValue());
        }


        @Test
        public void shouldRejectNullNoteText(){
            assertThrows(IllegalArgumentException.class, ()-> jobApplication.addNote(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t"})
        public void shouldRejectBlankNoteText(String text){
            assertThrows(IllegalArgumentException.class, ()-> jobApplication.addNote(text));
        }

        @Test
        public void shouldAllowDuplicateNotes(){
            jobApplication.addNote("Prepare to HR interview");
            jobApplication.addNote("Prepare to HR interview");
            assertEquals(2, jobApplication.getNotes().size());
        }

        @Test
        public void shouldKeepNotesInInsertionOrder(){
            jobApplication.addNote("Prepare to HR interview");
            jobApplication.addNote("Prepare to Technical Interview");
            assertEquals("Prepare to HR interview", jobApplication.getNotes().get(0).getValue());
            assertEquals("Prepare to Technical Interview", jobApplication.getNotes().get(1).getValue());
        }

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, mode= EnumSource.Mode.EXCLUDE, names = {"REJECTED", "WITHDRAWN"})
        public void shouldAllowNotesInAllowedStatuses(ApplicationStatus givenStatus){
            switch (givenStatus){
                case ApplicationStatus.APPLIED:
                    jobApplication.apply(LocalDate.now());
                    break;
                case ApplicationStatus.WISHLIST:
                    break;
                default:
                    jobApplication.apply(LocalDate.now());
                    jobApplication.moveTo(givenStatus);
                    break;
            }
            assertEquals(givenStatus, jobApplication.getStatus());
            assertDoesNotThrow(()-> jobApplication.addNote("Prepare to HR interview"));
            assertEquals(1, jobApplication.getNotes().size());
        }

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"REJECTED", "WITHDRAWN"})
        public void shouldRejectNoteWhenRejected(ApplicationStatus givenStatus){
            jobApplication.apply(LocalDate.now());
            jobApplication.moveTo(givenStatus);
            assertThrows(IllegalStateException.class, ()->jobApplication.addNote("Prepare to Interview"));
            assertEquals(0,jobApplication.getNotes().size());
        }
    }

    @Nested
    class deleteNote{
        @BeforeEach
        public void init(){
            jobApplication.addNote("Prepare to interview");
        }

        @Test
        public void shouldRejectNullNoteId(){
            assertThrows(IllegalArgumentException.class, ()->jobApplication.deleteNote(null));
        }

        @Test
        public void shouldThrowResourceNotFoundWhenNoteDoesNotExist(){
            assertThrows(ResourceNotFound.class, ()->jobApplication.deleteNote(5L));
        }

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"REJECTED", "WITHDRAWN"})
        public void shouldRejectDeleteWhenLocked(ApplicationStatus givenStatus){
            jobApplication.apply(LocalDate.now());
            jobApplication.moveTo(givenStatus);
            assertThrows(IllegalStateException.class, ()->jobApplication.deleteNote(1L));
        }
    }





}
