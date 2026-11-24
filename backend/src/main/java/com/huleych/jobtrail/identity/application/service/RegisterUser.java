package com.huleych.jobtrail.identity.application.service;

import com.huleych.jobtrail.identity.application.commands.RegisterUserCommand;
import com.huleych.jobtrail.identity.domain.valueObjects.Email;
import com.huleych.jobtrail.identity.domain.valueObjects.Name;
import com.huleych.jobtrail.identity.domain.valueObjects.Password;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.infrastructure.repository.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUser {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User execute(RegisterUserCommand command) {
        if (command == null) throw new IllegalArgumentException("Request body is required for registration");
        Name name = new Name(command.name());
        Email email = new Email(command.email());
        Password password = new Password(command.password());
        if (userRepository.existsByEmail(email)) throw new EmailAlreadyTakenException(email.value());
        String passwordHash = passwordEncoder.encode(password.value());
        try {
            return userRepository.save(new User(name.value(), email.value(), passwordHash, command.skills()));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyTakenException(email.value());
        }
    }
}
