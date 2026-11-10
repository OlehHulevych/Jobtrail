package com.huleych.jobtrail.identity.application.commands;

import jakarta.validation.constraints.NotNull;

public record RefreshCommand(@NotNull String refreshToken) {
}
