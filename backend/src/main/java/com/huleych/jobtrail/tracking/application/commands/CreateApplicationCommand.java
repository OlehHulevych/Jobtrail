package com.huleych.jobtrail.tracking.application.commands;

public record CreateApplicationCommand(Long ownerId, String company, String position, String url, String jobDescription){}
