package com.huleych.jobtrail.identity.application.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LoginCommand(@Email String email, @NotNull String password) {
}
