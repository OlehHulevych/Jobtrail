package com.huleych.jobtrail.identity.infrastructure;
import com.huleych.jobtrail.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailValue(String email);
    Optional<User> findByEmailValue(String email);
}
