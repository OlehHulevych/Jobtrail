package com.huleych.jobtrail.identity.application.commands;

import com.huleych.jobtrail.identity.domain.valueObjects.Skill;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record GithubRegisterRequest(@NotBlank String code, Set<Skill> skills) {

}
