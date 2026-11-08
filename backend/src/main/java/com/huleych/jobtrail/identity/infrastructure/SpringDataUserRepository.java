package com.huleych.jobtrail.identity.infrastructure;
import com.huleych.jobtrail.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailValue(String email);
    Optional<User> findByEmailValue(String email);
}
