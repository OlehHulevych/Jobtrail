package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.application.result.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GoogleRegisterRequest;
import com.huleych.jobtrail.identity.application.service.GoogleAuthService;
import com.huleych.jobtrail.identity.application.service.RefreshTokenService;
import com.huleych.jobtrail.identity.domain.valueObjects.Email;
import com.huleych.jobtrail.identity.domain.valueObjects.Skill;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.infrastructure.repository.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundByEmailException;
import com.huleych.jobtrail.identity.infrastructure.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthServiceTest {
    private static final String ID_TOKEN = "google-id-token";
    private static final String EMAIL = "user@example.com";
    private static final String NAME = "Test User";

    @Mock
    JwtDecoder googleJwtDecoder;
    @Mock
    UserRepository userRepository;
    @Mock
    TokenService tokenService;
    @Mock
    RefreshTokenService refreshTokenService;
    GoogleAuthService service;

    @BeforeEach
    public void init() {
        service = new GoogleAuthService(googleJwtDecoder, userRepository, tokenService, refreshTokenService);
    }

    private Jwt googleJwt(Boolean emailVerified) {
        Jwt.Builder builder = Jwt.withTokenValue(ID_TOKEN)
                .header("alg", "RS256")
                .claim("email", EMAIL)
                .claim("name", NAME);
        if (emailVerified != null) builder.claim("email_verified", emailVerified);
        return builder.build();
    }

    @Test
    public void loginShouldReturnTokensWhenUserExistsAndEmailIsVerified() {
        User user = new User(NAME, EMAIL, null, Set.of());
        when(googleJwtDecoder.decode(ID_TOKEN)).thenReturn(googleJwt(true));
        when(userRepository.findByEmail(new Email(EMAIL))).thenReturn(Optional.of(user));
        when(tokenService.createAccessToken(user)).thenReturn("access");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh");

        LoginResult result = service.login(ID_TOKEN);

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
    }

    @Test
    public void loginShouldThrowUserNotFoundByEmailWhenUserDoesNotExist() {
        when(googleJwtDecoder.decode(ID_TOKEN)).thenReturn(googleJwt(true));
        when(userRepository.findByEmail(new Email(EMAIL))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundByEmailException.class, () -> service.login(ID_TOKEN));
        verifyNoInteractions(tokenService, refreshTokenService);
    }

    @Test
    public void loginShouldReturnUnauthorizedWhenGoogleTokenIsInvalid() {
        when(googleJwtDecoder.decode(ID_TOKEN)).thenThrow(new JwtException("bad token"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.login(ID_TOKEN));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verifyNoInteractions(userRepository, tokenService, refreshTokenService);
    }

    @Test
    public void loginShouldReturnUnauthorizedWhenEmailIsNotVerified() {
        when(googleJwtDecoder.decode(ID_TOKEN)).thenReturn(googleJwt(false));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.login(ID_TOKEN));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verifyNoInteractions(userRepository, tokenService, refreshTokenService);
    }

    @Test
    public void loginShouldReturnUnauthorizedWhenEmailVerifiedClaimIsMissing() {
        when(googleJwtDecoder.decode(ID_TOKEN)).thenReturn(googleJwt(null));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.login(ID_TOKEN));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verifyNoInteractions(userRepository, tokenService, refreshTokenService);
    }

    @Test
    public void registerShouldSaveUserWithoutPasswordAndWithSkills() {
        Set<Skill> skills = Set.of(new Skill("java"));
        when(googleJwtDecoder.decode(ID_TOKEN)).thenReturn(googleJwt(true));
        when(userRepository.findByEmail(new Email(EMAIL))).thenReturn(Optional.empty());

        User result = service.register(new GoogleRegisterRequest(ID_TOKEN, skills));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertSame(result, saved.getValue());
        assertEquals(EMAIL, result.getEmail().value());
        assertNull(result.getPassword());
        assertEquals(skills, result.getSkills());
    }

    @Test
    public void registerShouldThrowEmailAlreadyTakenWhenUserExists() {
        when(googleJwtDecoder.decode(ID_TOKEN)).thenReturn(googleJwt(true));
        when(userRepository.findByEmail(new Email(EMAIL))).thenReturn(Optional.of(new User(NAME, EMAIL, "hash", Set.of())));

        assertThrows(EmailAlreadyTakenException.class,
                () -> service.register(new GoogleRegisterRequest(ID_TOKEN, Set.of())));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void registerShouldReturnUnauthorizedWhenGoogleTokenIsInvalid() {
        when(googleJwtDecoder.decode(ID_TOKEN)).thenThrow(new JwtException("bad token"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.register(new GoogleRegisterRequest(ID_TOKEN, Set.of())));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verifyNoInteractions(userRepository);
    }

    @Test
    public void registerShouldReturnUnauthorizedWhenEmailIsNotVerified() {
        when(googleJwtDecoder.decode(ID_TOKEN)).thenReturn(googleJwt(false));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.register(new GoogleRegisterRequest(ID_TOKEN, Set.of())));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verifyNoInteractions(userRepository);
    }
}
