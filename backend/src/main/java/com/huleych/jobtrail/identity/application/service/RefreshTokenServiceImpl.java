package com.huleych.jobtrail.identity.application.service;

import com.huleych.jobtrail.identity.domain.RefreshToken;
import com.huleych.jobtrail.identity.domain.RefreshTokenRepository;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.infrastructure.JwtProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JwtProperties properties;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository repo, JwtProperties props){
        this.refreshTokenRepository = repo;
        this.properties = props;
    }


    @Override
    public String createRefreshToken(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var token = new RefreshToken(user, hash(rawToken), LocalDateTime.now().plusDays(properties.refreshTtl().toDays()));
        refreshTokenRepository.save(token);
        return rawToken;
    }

    static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean isExpired(RefreshToken refreshToken) {
        return refreshToken.getExpiresAt().isBefore(LocalDateTime.now());
    }
}
