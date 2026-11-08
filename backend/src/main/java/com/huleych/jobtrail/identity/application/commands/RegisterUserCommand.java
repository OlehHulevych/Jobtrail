package com.huleych.jobtrail.identity.application.commands;

import com.huleych.jobtrail.identity.domain.Skill;

import java.util.Set;

public record RegisterUserCommand(String email, String password, Set<Skill> skills) {
}
