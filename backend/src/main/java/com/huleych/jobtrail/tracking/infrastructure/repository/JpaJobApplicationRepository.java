package com.huleych.jobtrail.tracking.infrastructure.repository;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    public Map<ApplicationStatus, Long> countByStatus(UUID ownerId) {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (Object[] row : repository.countByStatus(ownerId)) {
            counts.put((ApplicationStatus) row[0], (Long) row[1]);
        }
        return counts;
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
