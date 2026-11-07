package com.huleych.jobtrail.identity.domain;

public record Password(String value) {
    private static final String PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$";

    public Password {
        if (value == null) throw new IllegalArgumentException("Password is required");
        if (!value.matches(PATTERN))
            throw new IllegalArgumentException(
                    "Password must have at least 8 characters, one uppercase letter, one digit and one special symbol");
    }

    @Override
    public String toString() {
        return "Password[value=****]";
    }
}
