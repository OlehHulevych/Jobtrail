package com.huleych.jobtrail.identity.application.commands;

import com.huleych.jobtrail.identity.domain.Skill;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record GoogleRegisterCommand(@NotBlank String idToken, Set<Skill> skills ) {

}
