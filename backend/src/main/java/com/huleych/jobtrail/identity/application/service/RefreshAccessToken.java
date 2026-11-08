package com.huleych.jobtrail.identity.application.service;

import com.huleych.jobtrail.identity.application.LoginResult;
import com.huleych.jobtrail.identity.application.commands.RefreshCommand;
import com.huleych.jobtrail.identity.domain.RefreshToken;
import com.huleych.jobtrail.identity.domain.RefreshTokenRepository;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.domain.exceptions.InvalidCredentialsException;
import com.huleych.jobtrail.identity.infrastructure.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class RefreshAccessToken {
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public RefreshAccessToken(TokenService tokenService, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Transactional
    public LoginResult execute(RefreshCommand command) {
        if (command == null) throw new IllegalArgumentException("Request body is required for refresh");
        if (command.refreshToken() == null || command.refreshToken().isBlank()) throw new InvalidCredentialsException();
        RefreshToken stored = refreshTokenRepository.findByTokenHash(refreshTokenService.hash(command.refreshToken()))
                .orElseThrow(InvalidCredentialsException::new);
        LocalDateTime now = LocalDateTime.now(clock);
        if (!stored.isActive(now)) throw new InvalidCredentialsException();
        stored.revoke(now);
        User user = stored.getUser();
        String accessToken = tokenService.createAccessToken(user);
        String newRefreshToken = refreshTokenService.createRefreshToken(user);
        return new LoginResult(accessToken, newRefreshToken);
    }
}
