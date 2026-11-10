package com.huleych.jobtrail.identity.infrastructure;

import com.huleych.jobtrail.identity.domain.User;
import org.springframework.security.core.userdetails.UserDetails;


public interface TokenService {
   String createAccessToken(User user);
   String getUserEmail(String jwt);
   boolean tokenValid(String token, UserDetails details);
   boolean isTokenExpired(String token);

}
