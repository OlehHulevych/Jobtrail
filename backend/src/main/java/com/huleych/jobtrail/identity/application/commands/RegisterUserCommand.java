package com.huleych.jobtrail.identity.application.commands;

import com.huleych.jobtrail.identity.domain.Skill;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RegisterUserCommand(@NotNull String name, @NotNull @Email String email, @NotNull String password, Set<Skill> skills) {
}
