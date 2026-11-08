package com.huleych.jobtrail.identity.application.service;

import com.huleych.jobtrail.identity.domain.RefreshToken;
import com.huleych.jobtrail.identity.domain.User;

public interface RefreshTokenService {
    public String createRefreshToken(User user);
    public boolean isExpired(RefreshToken refreshToken);
    public String hash(String rawToken);
}
