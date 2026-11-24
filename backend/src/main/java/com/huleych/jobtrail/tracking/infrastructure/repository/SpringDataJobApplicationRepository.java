package com.huleych.jobtrail.tracking.infrastructure.repository;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataJobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    List<JobApplication> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    List<JobApplication> findAllByOwnerIdAndStatusOrderByCreatedAtDesc(UUID ownerId, ApplicationStatus status);
}
