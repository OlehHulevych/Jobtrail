package com.huleych.jobtrail.tracking.application;

public record CreateApplicationCommand(Long ownerId, String company, String position, String url, String jobDescription){}
