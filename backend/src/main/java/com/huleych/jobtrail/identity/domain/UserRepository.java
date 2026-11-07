package com.huleych.jobtrail.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository{
    User save(User user);
    boolean existsByEmail(Email email);
}
