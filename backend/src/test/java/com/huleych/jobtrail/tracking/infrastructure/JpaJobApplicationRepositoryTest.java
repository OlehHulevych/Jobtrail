package com.huleych.jobtrail.tracking.infrastructure;

import com.huleych.jobtrail.support.PostgresContainerConfig;
import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.Note;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresContainerConfig.class, JpaJobApplicationRepository.class})
class JpaJobApplicationRepositoryTest {

    private static final LocalDate APPLIED_DATE = LocalDate.of(2026, 1, 15);

    @Autowired
    JpaJobApplicationRepository repository;
    @Autowired
    EntityManager entityManager;

    private JobApplication newApplication(Long ownerId, String company) {
        return new JobApplication(ownerId, company, "Java Developer", null, null);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldPersistAndLoadAllFields() {
        JobApplication application = new JobApplication(1L, "Acme", "Java Developer",
                "https://acme.example/jobs/1", "Build backend services");
        Long id = repository.save(application).getId();
        flushAndClear();

        JobApplication loaded = repository.findById(id).orElseThrow();

        assertEquals(1L, loaded.getOwnerId());
        assertEquals("Acme", loaded.getCompany());
        assertEquals("Java Developer", loaded.getPosition());
        assertEquals("https://acme.example/jobs/1", loaded.getUrl());
        assertEquals("Build backend services", loaded.getJobDescription());
        assertEquals(ApplicationStatus.WISHLIST, loaded.getStatus());
        assertNull(loaded.getAppliedAt());
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        assertTrue(repository.findById(999_999L).isEmpty());
    }

    @Test
    void shouldReturnOnlyApplicationsOfGivenOwner() {
        repository.save(newApplication(1L, "Acme"));
        repository.save(newApplication(1L, "Globex"));
        repository.save(newApplication(2L, "Initech"));
        flushAndClear();

        List<JobApplication> result = repository.findAllByOwnerId(1L);

        assertEquals(2, result.size());
        assertEquals(Set.of("Acme", "Globex"),
                Set.copyOf(result.stream().map(JobApplication::getCompany).toList()));
    }

    @Test
    void shouldFilterByStatus() {
        JobApplication applied = newApplication(1L, "Globex");
        applied.apply(APPLIED_DATE);
        repository.save(newApplication(1L, "Acme"));
        repository.save(applied);
        flushAndClear();

        List<JobApplication> result = repository.findAllByOwnerIdAndStatus(1L, ApplicationStatus.APPLIED);

        assertEquals(1, result.size());
        assertEquals("Globex", result.getFirst().getCompany());
        assertEquals(ApplicationStatus.APPLIED, result.getFirst().getStatus());
    }

    @Test
    void shouldReturnNewestFirst() {
        Long acme = repository.save(newApplication(1L, "Acme")).getId();
        Long globex = repository.save(newApplication(1L, "Globex")).getId();
        Long initech = repository.save(newApplication(1L, "Initech")).getId();
        entityManager.flush();
        setCreatedAt(acme, LocalDateTime.of(2026, 1, 1, 10, 0));
        setCreatedAt(globex, LocalDateTime.of(2026, 1, 3, 10, 0));
        setCreatedAt(initech, LocalDateTime.of(2026, 1, 2, 10, 0));
        entityManager.clear();

        List<String> companies = repository.findAllByOwnerId(1L).stream()
                .map(JobApplication::getCompany).toList();

        assertEquals(List.of("Globex", "Initech", "Acme"), companies);
    }

    @Test
    void shouldStoreStatusAsText() {
        JobApplication application = newApplication(1L, "Acme");
        application.apply(APPLIED_DATE);
        Long id = repository.save(application).getId();
        entityManager.flush();

        Object rawStatus = entityManager
                .createNativeQuery("select status from job_applications where id = ?1")
                .setParameter(1, id)
                .getSingleResult();

        assertEquals("APPLIED", rawStatus);
    }

    @Test
    void shouldPersistStatusChange() {
        Long id = repository.save(newApplication(1L, "Acme")).getId();
        flushAndClear();

        JobApplication loaded = repository.findById(id).orElseThrow();
        loaded.apply(APPLIED_DATE);
        repository.save(loaded);
        flushAndClear();

        JobApplication reloaded = repository.findById(id).orElseThrow();
        assertEquals(ApplicationStatus.APPLIED, reloaded.getStatus());
        assertEquals(APPLIED_DATE, reloaded.getAppliedAt());
    }

    @Test
    void shouldRejectRowWithoutCompanyAtDatabaseLevel() {
        assertThrows(PersistenceException.class, () -> entityManager
                .createNativeQuery("insert into job_applications (owner_id, company, position) values (1, null, 'Dev')")
                .executeUpdate());
    }
    @Test
    void shouldPersistAndLoadNotes() {
        JobApplication application = newApplication(1L, "Acme");
        application.addNote("Prepare for HR interview");
        Long id = repository.save(application).getId();
        flushAndClear();

        JobApplication loaded = repository.findById(id).orElseThrow();

        assertEquals(1, loaded.getNotes().size());
        assertEquals("Prepare for HR interview", loaded.getNotes().getFirst().getValue());
    }

    @Test
    void shouldReturnNotesOrderedByCreationTime() {
        JobApplication application = newApplication(1L, "Acme");
        application.addNote("first");
        application.addNote("second");
        application.addNote("third");
        Long id = repository.save(application).getId();
        entityManager.flush();
        List<Note> saved = application.getNotes();
        setNoteCreatedAt(saved.get(0).getId(), LocalDateTime.of(2026, 1, 3, 10, 0));
        setNoteCreatedAt(saved.get(1).getId(), LocalDateTime.of(2026, 1, 1, 10, 0));
        setNoteCreatedAt(saved.get(2).getId(), LocalDateTime.of(2026, 1, 2, 10, 0));
        entityManager.clear();

        List<String> values = repository.findById(id).orElseThrow().getNotes().stream()
                .map(Note::getValue).toList();

        assertEquals(List.of("second", "third", "first"), values);
    }
    @Test
    void shouldDeleteNoteRowWhenRemovedFromApplication() {
        JobApplication application = newApplication(1L, "Acme");
        application.addNote("to delete");
        Long id = repository.save(application).getId();
        entityManager.flush();
        Long noteId = application.getNotes().getFirst().getId();
        entityManager.clear();

        JobApplication loaded = repository.findById(id).orElseThrow();
        loaded.deleteNote(noteId);
        repository.save(loaded);
        entityManager.flush();

        assertEquals(0L, countNotes(id));
    }

    private void setNoteCreatedAt(Long id, LocalDateTime createdAt) {
        entityManager.createNativeQuery("update notes set created_at = ?1 where id = ?2")
                .setParameter(1, createdAt)
                .setParameter(2, id)
                .executeUpdate();
    }

    private long countNotes(Long applicationId) {
        return ((Number) entityManager
                .createNativeQuery("select count(*) from notes where job_application_id = ?1")
                .setParameter(1, applicationId)
                .getSingleResult()).longValue();
    }

    private void setCreatedAt(Long id, LocalDateTime createdAt) {
        entityManager.createNativeQuery("update job_applications set created_at = ?1 where id = ?2")
                .setParameter(1, createdAt)
                .setParameter(2, id)
                .executeUpdate();
    }


}