package com.huleych.jobtrail.tracking.application.commands;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;

public record ChangeStatusCommand (Long id,Long ownerId, ApplicationStatus status){}
