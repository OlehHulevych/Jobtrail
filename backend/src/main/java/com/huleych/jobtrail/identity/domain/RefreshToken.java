package com.huleych.jobtrail.identity.domain;

import com.huleych.jobtrail.tracking.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    protected RefreshToken() {

    }

    public RefreshToken(User user, String tokenHash, LocalDateTime expiresAt) {
        if (user == null) throw new IllegalArgumentException("User is required");
        if (tokenHash == null || tokenHash.isBlank()) throw new IllegalArgumentException("Token hash is required");
        if (expiresAt == null) throw new IllegalArgumentException("Expiry is required");
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive(LocalDateTime now) {
        return !isRevoked() && !isExpired(now);
    }

    public void revoke(LocalDateTime now) {
        if (revokedAt == null) revokedAt = now;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
}
