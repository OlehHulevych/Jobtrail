package com.huleych.jobtrail.identity.infrastructure.repository;

import com.huleych.jobtrail.identity.domain.valueObjects.Email;
import com.huleych.jobtrail.identity.domain.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepository implements UserRepository{
    private final SpringDataUserRepository repository;

    public JpaUserRepository(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmailValue(email.value());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return repository.findByEmailValue(email.value());
    }
}
