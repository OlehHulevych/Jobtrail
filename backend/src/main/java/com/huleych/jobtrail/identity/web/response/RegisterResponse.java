package com.huleych.jobtrail.identity.web.response;

import java.util.UUID;

public record RegisterResponse(UUID id, String name, String email) {
}
