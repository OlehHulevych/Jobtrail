package com.huleych.jobtrail.tracking.domain;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository {

    JobApplication save(JobApplication jobApplication);
    List<JobApplication> findAllByOwnerId(Long ownerId);
    List<JobApplication> findAllByOwnerIdAndStatus(Long ownerId, ApplicationStatus status);
    Optional<JobApplication> findById(Long id);

}
