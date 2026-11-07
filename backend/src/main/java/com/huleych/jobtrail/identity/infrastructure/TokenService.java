package com.huleych.jobtrail.identity.infrastructure;

import com.huleych.jobtrail.identity.domain.User;


public interface TokenService {
   String createAccessToken(User user);
}
