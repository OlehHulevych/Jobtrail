package com.huleych.jobtrail.tracking.infrastructure.repository;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataJobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    List<JobApplication> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    List<JobApplication> findAllByOwnerIdAndStatusOrderByCreatedAtDesc(UUID ownerId, ApplicationStatus status);

    @Query("select a.status, count(a) from JobApplication a where a.ownerId = :ownerId group by a.status")
    List<Object[]> countByStatus(@Param("ownerId") UUID ownerId);
}
