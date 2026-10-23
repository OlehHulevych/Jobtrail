package com.huleych.jobtrail.tracking.application.commands;

public record UpdateJobApplicationCommand(Long id, Long ownerId, String company, String position, String url, String jobDescription) { }
