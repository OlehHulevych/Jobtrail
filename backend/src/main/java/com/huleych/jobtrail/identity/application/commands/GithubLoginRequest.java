package com.huleych.jobtrail.identity.application.commands;

import jakarta.validation.constraints.NotBlank;

public record GithubLoginRequest(@NotBlank String code) {
}
