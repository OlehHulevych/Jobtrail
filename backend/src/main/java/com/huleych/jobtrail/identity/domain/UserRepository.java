package com.huleych.jobtrail.identity.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository{
    User save(User user);
    boolean existsById(UUID id);
    boolean existsByEmail(Email email);
    Optional<User> findByEmail(Email email);
}
