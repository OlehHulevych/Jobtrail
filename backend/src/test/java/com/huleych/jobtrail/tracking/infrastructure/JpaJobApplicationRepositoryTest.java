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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresContainerConfig.class, JpaJobApplicationRepository.class})
class JpaJobApplicationRepositoryTest {

    private static final LocalDate APPLIED_DATE = LocalDate.of(2026, 1, 15);
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    JpaJobApplicationRepository repository;
    @Autowired
    EntityManager entityManager;

    private JobApplication newApplication(UUID ownerId, String company) {
        return new JobApplication(ownerId, company, "Java Developer", null, null);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldPersistAndLoadAllFields() {
        JobApplication application = new JobApplication(OWNER_ID, "Acme", "Java Developer",
                "https://acme.example/jobs/1", "Build backend services");
        UUID id =repository.save(application).getId();
        flushAndClear();

        JobApplication loaded = repository.findById(id).orElseThrow();

        assertEquals(OWNER_ID, loaded.getOwnerId());
        assertEquals("Acme", loaded.getCompany());
        assertEquals("Java Developer", loaded.getPosition());
        assertEquals("https://acme.example/jobs/1", loaded.getUrl());
        assertEquals("Build backend services", loaded.getJobDescription());
        assertEquals(ApplicationStatus.WISHLIST, loaded.getStatus());
        assertNull(loaded.getAppliedAt());
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void shouldReturnOnlyApplicationsOfGivenOwner() {
        repository.save(newApplication(OWNER_ID, "Acme"));
        repository.save(newApplication(OWNER_ID, "Globex"));
        repository.save(newApplication(OTHER_OWNER_ID, "Initech"));
        flushAndClear();

        List<JobApplication> result = repository.findAllByOwnerId(OWNER_ID);

        assertEquals(2, result.size());
        assertEquals(Set.of("Acme", "Globex"),
                Set.copyOf(result.stream().map(JobApplication::getCompany).toList()));
    }

    @Test
    void shouldFilterByStatus() {
        JobApplication applied = newApplication(OWNER_ID, "Globex");
        applied.apply(APPLIED_DATE);
        repository.save(newApplication(OWNER_ID, "Acme"));
        repository.save(applied);
        flushAndClear();

        List<JobApplication> result = repository.findAllByOwnerIdAndStatus(OWNER_ID, ApplicationStatus.APPLIED);

        assertEquals(1, result.size());
        assertEquals("Globex", result.getFirst().getCompany());
        assertEquals(ApplicationStatus.APPLIED, result.getFirst().getStatus());
    }

    @Test
    void shouldReturnNewestFirst() {
        UUID acme = repository.save(newApplication(OWNER_ID, "Acme")).getId();
        UUID globex = repository.save(newApplication(OWNER_ID, "Globex")).getId();
        UUID initech =repository.save(newApplication(OWNER_ID, "Initech")).getId();
        entityManager.flush();
        setCreatedAt(acme, LocalDateTime.of(2026, 1, 1, 10, 0));
        setCreatedAt(globex, LocalDateTime.of(2026, 1, 3, 10, 0));
        setCreatedAt(initech, LocalDateTime.of(2026, 1, 2, 10, 0));
        entityManager.clear();

        List<String> companies = repository.findAllByOwnerId(OWNER_ID).stream()
                .map(JobApplication::getCompany).toList();

        assertEquals(List.of("Globex", "Initech", "Acme"), companies);
    }

    @Test
    void shouldStoreStatusAsText() {
        JobApplication application = newApplication(OWNER_ID, "Acme");
        application.apply(APPLIED_DATE);
        UUID id =repository.save(application).getId();
        entityManager.flush();

        Object rawStatus = entityManager
                .createNativeQuery("select status from job_applications where id = ?1")
                .setParameter(1, id)
                .getSingleResult();

        assertEquals("APPLIED", rawStatus);
    }

    @Test
    void shouldPersistStatusChange() {
        UUID id =repository.save(newApplication(OWNER_ID, "Acme")).getId();
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
                .createNativeQuery("insert into job_applications (id, owner_id, company, position) values (gen_random_uuid(), gen_random_uuid(), null, 'Dev')")
                .executeUpdate());
    }
    @Test
    void shouldPersistAndLoadNotes() {
        JobApplication application = newApplication(OWNER_ID, "Acme");
        application.addNote("Prepare for HR interview");
        UUID id =repository.save(application).getId();
        flushAndClear();

        JobApplication loaded = repository.findById(id).orElseThrow();

        assertEquals(1, loaded.getNotes().size());
        assertEquals("Prepare for HR interview", loaded.getNotes().getFirst().getValue());
    }

    @Test
    void shouldReturnNotesOrderedByCreationTime() {
        JobApplication application = newApplication(OWNER_ID, "Acme");
        application.addNote("first");
        application.addNote("second");
        application.addNote("third");
        UUID id =repository.save(application).getId();
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
        JobApplication application = newApplication(OWNER_ID, "Acme");
        application.addNote("to delete");
        UUID id =repository.save(application).getId();
        entityManager.flush();
        UUID noteId =application.getNotes().getFirst().getId();


        JobApplication loaded = repository.findById(id).orElseThrow();
        loaded.deleteNote(noteId);
        repository.save(loaded);
        entityManager.flush();
        entityManager.clear();


        assertEquals(0L, countNotes(id));
    }

    @Test
    void shouldDeleteOnlyTheRequestedNote(){
        JobApplication application = new JobApplication(OWNER_ID, "Acme", "Java Developer",
                "https://acme.example/jobs/1", "Build backend services");
        application.addNote("to delete");
        application.addNote("to delete II");
        UUID id =repository.save(application).getId();
        entityManager.flush();
        entityManager.clear();
        UUID noteId =application.getNotes().getFirst().getId();
        JobApplication loaded = repository.findById(id).orElseThrow();
        loaded.deleteNote(noteId);
        repository.save(loaded);
        entityManager.flush();
        entityManager.clear();
        JobApplication newLoaded = repository.findById(id).orElseThrow();
        assertEquals("to delete II",newLoaded.getNotes().getFirst().getValue());
    }

    @Test
    void shouldAllowDeleteInOffer(){
        JobApplication application = new JobApplication(OWNER_ID, "Acme", "Java Developer",
                "https://acme.example/jobs/1", "Build backend services");
        application.apply(LocalDate.now());
        application.moveTo(ApplicationStatus.OFFER);
        application.addNote("to delete");
        UUID id =repository.save(application).getId();
        entityManager.flush();
        UUID noteId =application.getNotes().getFirst().getId();
        JobApplication loaded = repository.findById(id).orElseThrow();
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, loaded.getNotes().size());
        assertEquals(ApplicationStatus.OFFER,loaded.getStatus());
        loaded.deleteNote(noteId);
        repository.save(loaded);
        entityManager.flush();
        entityManager.clear();
        JobApplication newLoaded = repository.findById(id).orElseThrow();
        assertEquals(0,newLoaded.getNotes().size());

    }

    private void setNoteCreatedAt(UUID id, LocalDateTime createdAt) {
        entityManager.createNativeQuery("update notes set created_at = ?1 where id = ?2")
                .setParameter(1, createdAt)
                .setParameter(2, id)
                .executeUpdate();
    }

    private long countNotes(UUID applicationId) {
        return ((Number) entityManager
                .createNativeQuery("select count(*) from notes where job_application_id = ?1")
                .setParameter(1, applicationId)
                .getSingleResult()).longValue();
    }

    private void setCreatedAt(UUID id, LocalDateTime createdAt) {
        entityManager.createNativeQuery("update job_applications set created_at = ?1 where id = ?2")
                .setParameter(1, createdAt)
                .setParameter(2, id)
                .executeUpdate();
    }


}