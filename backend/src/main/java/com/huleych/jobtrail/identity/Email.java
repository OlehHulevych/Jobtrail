package com.huleych.jobtrail.identity;

import jakarta.persistence.Embeddable;

import java.util.Locale;


@Embeddable
public record Email(String value) {
    public Email {
        if (value == null) throw new IllegalArgumentException("Email is required");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Incorrect email format");
    }
}
