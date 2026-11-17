package com.huleych.jobtrail.identity.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import java.util.List;

@Configuration
public class GoogleJwtConfig {

    @Bean
    JwtDecoder googleJwtDecoder(@Value("${google.client-id}") String clientId){
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();

        OAuth2TokenValidator<Jwt> issuer = jwt ->{
            String iss = jwt.getIssuer()==null ? "" : jwt.getIssuer().toString();
            return (iss.equals("https://accounts.google.com") || iss.equals("accounts.google.com"))
                    ? OAuth2TokenValidatorResult.success():
                    OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_issuer"));
        };
        OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<List<String>>("aud", aud-> aud.contains(clientId));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), issuer, audience));
        return decoder;
    }
}
