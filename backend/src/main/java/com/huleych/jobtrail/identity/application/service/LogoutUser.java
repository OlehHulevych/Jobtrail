package com.huleych.jobtrail.identity.application.service;
import com.huleych.jobtrail.identity.domain.RefreshToken;
import com.huleych.jobtrail.identity.domain.RefreshTokenRepository;
import com.huleych.jobtrail.identity.domain.exceptions.RefreshTokenNotFoundException;
import com.huleych.jobtrail.identity.application.commands.LogoutCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class LogoutUser {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final Clock clock;

    public LogoutUser(RefreshTokenRepository refreshTokenRepository, RefreshTokenService refreshTokenService, Clock clock){
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenService = refreshTokenService;
        this.clock = clock;
    }

    @Transactional
    public void execute(LogoutCommand command){
        if(command == null) throw new IllegalArgumentException("Logout command is required");
        if (command.refreshToken() == null || command.refreshToken().isBlank()) throw new
                IllegalArgumentException("Refresh token is required");
        String hash = refreshTokenService.hash(command.refreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash).orElse(null);
        if(token==null) throw new RefreshTokenNotFoundException();
        token.revoke(LocalDateTime.now(clock));
    }
}
