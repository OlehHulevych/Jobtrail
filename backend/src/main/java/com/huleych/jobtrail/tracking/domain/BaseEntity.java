package com.huleych.jobtrail.tracking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue
    protected Long id;
    @CreationTimestamp
    protected LocalDateTime CreatedAt;
    @UpdateTimestamp
    protected LocalDateTime UpdatedAt;




}
