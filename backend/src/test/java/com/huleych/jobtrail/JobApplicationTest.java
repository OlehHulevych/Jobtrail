package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.exceptions.InvalidStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
        private JobApplication nestedJobApplication;

        @BeforeEach
        public void init(){
            nestedJobApplication = new JobApplication(5L, "Acme", "Java Developer", "https://acme.example/jobs/1",  "Build backend services with Spring Boot");
        }

        @Test
        public void  shouldAddNoteToNewApplication(){
            nestedJobApplication.addNote("Prepare to HR interview");
            assertEquals(1, nestedJobApplication.getNotes().size());
        }

        @Test
        public void shouldTrimNoteText(){
            nestedJobApplication.addNote("   Prepare to HR interview   ");
            assertEquals("Prepare to HR interview", nestedJobApplication.getNotes().getFirst().getValue());
        }


        @Test
        public void shouldRejectNullNoteText(){
            assertThrows(IllegalArgumentException.class, ()-> nestedJobApplication.addNote(null));
        }

        @Test
        public void shouldRejectBlankNoteText(){
            assertThrows(IllegalArgumentException.class, ()-> nestedJobApplication.addNote(" "));
        }

        @Test
        public void shouldAllowDuplicateNotes(){
            nestedJobApplication.addNote("Prepare to HR interview");
            nestedJobApplication.addNote("Prepare to HR interview");
            assertEquals(2, nestedJobApplication.getNotes().size());
        }

        @Test
        public void shouldKeepNotesInInsertionOrder(){
            nestedJobApplication.addNote("Prepare to HR interview");
            nestedJobApplication.addNote("Prepare to Technical Interview");
            assertEquals("Prepare to HR interview", nestedJobApplication.getNotes().get(0).getValue());
            assertEquals("Prepare to Technical Interview", nestedJobApplication.getNotes().get(1).getValue());
        }

        @Test
        public void shouldAllowNotesInAllowedStatuses(){
            nestedJobApplication.apply(LocalDate.now());
            assertDoesNotThrow(()-> nestedJobApplication.addNote("Prepare to HR interview"));
            nestedJobApplication.moveTo(ApplicationStatus.INTERVIEW);
            assertDoesNotThrow(()-> nestedJobApplication.addNote("Prepare to HR interview"));
            nestedJobApplication.moveTo(ApplicationStatus.OFFER);
            assertDoesNotThrow(()-> nestedJobApplication.addNote("Prepare to HR interview"));
        }

        @Test
        public void shouldRejectNoteWhenRejected(){
            nestedJobApplication.apply(LocalDate.now());
            nestedJobApplication.moveTo(ApplicationStatus.REJECTED);
            assertThrows(IllegalStateException.class, ()->nestedJobApplication.addNote("Prepare to Interview"));

        }

    }


}
