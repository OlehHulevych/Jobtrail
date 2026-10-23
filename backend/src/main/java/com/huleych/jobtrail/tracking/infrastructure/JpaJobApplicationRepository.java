package com.huleych.jobtrail.tracking.infrastructure;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public List<JobApplication> findAllByOwnerId(Long ownerId) {
        return repository.findAllByOwnerId(ownerId);
    }

    @Override
    public List<JobApplication> findAllByOwnerIdAndStatus(Long ownerId, ApplicationStatus status) {
        return repository.findAllByOwnerIdAndStatus(ownerId,status);
    }

    @Override
    public Optional<JobApplication> findById(Long id) {
        return repository.findById(id);
    }
}
