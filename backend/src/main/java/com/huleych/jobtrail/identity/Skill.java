package com.huleych.jobtrail.tracking.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Skill(String name) {
    public Skill{
        if (name ==null || name.isBlank()) throw new IllegalArgumentException("Skill name is required");
        name = name.trim();

    }

    public boolean matches (String other){
        return name.equalsIgnoreCase(other.trim());
    }
}
