package com.huleych.jobtrail.identity;

import jakarta.persistence.Embeddable;

import java.util.Locale;

@Embeddable
public record Skill(String name) {
    public Skill {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Skill name is required");
        name = name.trim().toLowerCase(Locale.ROOT);
    }

    public boolean matches(String other) {
        return other != null && name.equalsIgnoreCase(other.trim());
    }
}
