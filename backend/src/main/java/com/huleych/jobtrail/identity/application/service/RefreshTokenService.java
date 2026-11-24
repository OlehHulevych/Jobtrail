package com.huleych.jobtrail.identity.application.service;

import com.huleych.jobtrail.identity.domain.entity.RefreshToken;
import com.huleych.jobtrail.identity.domain.entity.User;

public interface RefreshTokenService {
    public String createRefreshToken(User user);
    public boolean isExpired(RefreshToken refreshToken);
    public String hash(String rawToken);
}
