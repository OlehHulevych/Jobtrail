package com.huleych.jobtrail.tracking.application;

public record UpdateJobApplicationCommand(Long id, Long ownerId, String company, String position, String url, String jobDescription) { }
