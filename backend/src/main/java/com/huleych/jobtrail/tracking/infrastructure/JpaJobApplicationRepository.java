package com.huleych.jobtrail.tracking.infrastructure;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
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
