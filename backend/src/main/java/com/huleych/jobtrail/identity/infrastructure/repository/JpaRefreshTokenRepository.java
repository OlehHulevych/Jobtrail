package com.huleych.jobtrail.identity.infrastructure.repository;

import com.huleych.jobtrail.identity.domain.entity.RefreshToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {
    private final SpringDataRefreshTokenRepository repository;

    public JpaRefreshTokenRepository(SpringDataRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return repository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash);
    }
}
