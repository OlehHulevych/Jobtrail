package com.huleych.jobtrail.tracking.infrastructure.repository;

import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;

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
