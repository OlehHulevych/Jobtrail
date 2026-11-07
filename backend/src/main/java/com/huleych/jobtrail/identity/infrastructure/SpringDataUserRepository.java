package com.huleych.jobtrail.identity.infrastructure;
import com.huleych.jobtrail.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailValue(String email);
}
