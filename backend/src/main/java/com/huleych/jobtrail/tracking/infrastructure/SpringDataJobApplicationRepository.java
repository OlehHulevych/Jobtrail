package com.huleych.jobtrail.tracking.infrastructure;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<JobApplication> findAllByOwnerIdAndStatusOrderByCreatedAtDesc(Long ownerId, ApplicationStatus status);
}
