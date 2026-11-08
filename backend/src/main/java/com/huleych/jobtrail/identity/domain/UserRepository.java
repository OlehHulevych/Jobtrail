package com.huleych.jobtrail.identity.domain;

import java.util.Optional;

public interface UserRepository{
    User save(User user);
    boolean existsByEmail(Email email);
    Optional<User> findByEmail(Email email);
}
