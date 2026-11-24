package com.huleych.jobtrail.tracking.application.commands;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;

import java.util.UUID;

public record ChangeStatusCommand (UUID applicationId, UUID ownerId, ApplicationStatus status){ }
