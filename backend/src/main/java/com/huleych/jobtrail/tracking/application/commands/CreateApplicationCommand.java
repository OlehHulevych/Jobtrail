package com.huleych.jobtrail.tracking.application.commands;

import java.util.UUID;

public record CreateApplicationCommand(UUID ownerId, String company, String position, String url, String jobDescription){}
