package com.huleych.jobtrail.identity.infrastructure;

import com.huleych.jobtrail.identity.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Component
public class TokenServiceImpl implements TokenService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwtProperties props;
    private final Clock clock;

    public TokenServiceImpl(JwtEncoder encoder,JwtDecoder decoder, JwtProperties props, Clock clock) {
        this.encoder = encoder;
        this.props = props;
        this.clock = clock;
        this.decoder = decoder;
    }

    @Override
    public String createAccessToken(User user) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("jobtrail")
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail().value())
                .issuedAt(now)
                .expiresAt(now.plus(props.accessTtl()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();
    }

    @Override
    public String getUserEmail(String jwt) {
        Jwt jwtString = decoder.decode(jwt);
        return jwtString.getClaim("email");
    }

    @Override
    public boolean tokenValid(String token, UserDetails details) {
        return Objects.equals(this.getUserEmail(token), details.getUsername()) && !isTokenExpired(token);
    }

    @Override
    public boolean isTokenExpired(String token) {
        Jwt jwtString = decoder.decode(token);
        assert jwtString.getExpiresAt() != null;
        return jwtString.getExpiresAt().isBefore(Instant.now());
    }


}
