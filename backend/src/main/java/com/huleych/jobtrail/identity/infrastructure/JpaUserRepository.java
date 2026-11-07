package com.huleych.jobtrail.identity.infrastructure;

import com.huleych.jobtrail.identity.domain.Email;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.domain.UserRepository;
import org.springframework.stereotype.Repository;

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
    public boolean existsByEmail(Email email) {
        return repository.existsByEmailValue(email.value());
    }
}
