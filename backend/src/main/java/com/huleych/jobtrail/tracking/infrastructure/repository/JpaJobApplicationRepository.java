package com.huleych.jobtrail.tracking.infrastructure.repository;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaJobApplicationRepository implements JobApplicationRepository {
    private final SpringDataJobApplicationRepository repository;

    public JpaJobApplicationRepository(SpringDataJobApplicationRepository repository){
        this.repository = repository;
    }
    @Override
    public JobApplication save(JobApplication jobApplication) {
        return repository.save(jobApplication);

    }

    @Override
    public List<JobApplication> findAllByOwnerId(UUID ownerId) {
        return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Override
    public List<JobApplication> findAllByOwnerIdAndStatus(UUID ownerId, ApplicationStatus status) {
        return repository.findAllByOwnerIdAndStatusOrderByCreatedAtDesc(ownerId,status);
    }

    @Override
    public Optional<JobApplication> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(JobApplication jobApplication) {
        repository.delete(jobApplication);
    }
}
