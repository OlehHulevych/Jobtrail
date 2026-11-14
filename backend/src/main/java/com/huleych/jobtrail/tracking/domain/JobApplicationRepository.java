package com.huleych.jobtrail.tracking.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository {

    JobApplication save(JobApplication jobApplication);
    List<JobApplication> findAllByOwnerId(UUID ownerId);
    List<JobApplication> findAllByOwnerIdAndStatus(UUID ownerId, ApplicationStatus status);
    Optional<JobApplication> findById(UUID id);
    void delete(JobApplication jobApplication);

}
