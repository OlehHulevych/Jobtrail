package com.huleych.jobtrail.tracking.application.commands;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;

public record ChangeStatusCommand (Long applicationId,Long ownerId, ApplicationStatus status){ }
