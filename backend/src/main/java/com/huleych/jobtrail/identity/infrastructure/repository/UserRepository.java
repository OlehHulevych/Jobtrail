package com.huleych.jobtrail.identity.infrastructure.repository;

import com.huleych.jobtrail.identity.domain.valueObjects.Email;
import com.huleych.jobtrail.identity.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository{
    User save(User user);
    boolean existsById(UUID id);
    boolean existsByEmail(Email email);
    Optional<User> findByEmail(Email email);
    Optional<User> findById(UUID id);
}
