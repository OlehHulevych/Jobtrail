package com.huleych.jobtrail.identity.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Name(String value) {
    public static final int MAX_LENGTH = 100;

    public Name {
        if (value == null) throw new IllegalArgumentException("Name is required");
        value = value.trim();
        if (value.isEmpty()) throw new IllegalArgumentException("Name must not be blank");
        if (value.length() > MAX_LENGTH)
            throw new IllegalArgumentException("Name must be at most " + MAX_LENGTH + " characters");
    }
}
