package com.huleych.jobtrail;

import com.huleych.jobtrail.identity.application.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GithubRegisterRequest;
import com.huleych.jobtrail.identity.application.service.GithubAuthService;
import com.huleych.jobtrail.identity.application.service.RefreshTokenService;
import com.huleych.jobtrail.identity.domain.Email;
import com.huleych.jobtrail.identity.domain.Skill;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.domain.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import com.huleych.jobtrail.identity.domain.exceptions.GithubAuthenticationException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundByEmailException;
import com.huleych.jobtrail.identity.infrastructure.GithubClient;
import com.huleych.jobtrail.identity.infrastructure.GithubProfile;
import com.huleych.jobtrail.identity.infrastructure.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GithubAuthServiceTest {
    private static final String CODE = "github-code";
    private static final String EMAIL = "user@example.com";
    private static final String NAME = "Test User";

    @Mock
    GithubClient githubClient;
    @Mock
    UserRepository userRepository;
    @Mock
    TokenService tokenService;
    @Mock
    RefreshTokenService refreshTokenService;
    GithubAuthService service;

    @BeforeEach
    public void init() {
        service = new GithubAuthService(githubClient, userRepository, tokenService, refreshTokenService);
    }

    @Test
    public void loginShouldReturnTokensWhenUserExists() {
        User user = new User(NAME, EMAIL, null, Set.of());
        when(githubClient.fetchProfile(CODE)).thenReturn(new GithubProfile(EMAIL, NAME));
        when(userRepository.findByEmail(new Email(EMAIL))).thenReturn(Optional.of(user));
        when(tokenService.createAccessToken(user)).thenReturn("access");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh");

        LoginResult result = service.login(CODE);

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
    }

    @Test
    public void loginShouldThrowUserNotFoundByEmailWhenUserDoesNotExist() {
        when(githubClient.fetchProfile(CODE)).thenReturn(new GithubProfile(EMAIL, NAME));
        when(userRepository.findByEmail(new Email(EMAIL))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundByEmailException.class, () -> service.login(CODE));
        verifyNoInteractions(tokenService, refreshTokenService);
    }

    @Test
    public void loginShouldPropagateGithubAuthenticationException() {
        when(githubClient.fetchProfile(CODE)).thenThrow(new GithubAuthenticationException("bad code"));

        assertThrows(GithubAuthenticationException.class, () -> service.login(CODE));
        verifyNoInteractions(userRepository, tokenService, refreshTokenService);
    }

    @Test
    public void registerShouldSaveUserWithoutPasswordAndWithSkills() {
        Set<Skill> skills = Set.of(new Skill("java"));
        when(githubClient.fetchProfile(CODE)).thenReturn(new GithubProfile(EMAIL, NAME));
        when(userRepository.findByEmail(new Email(EMAIL))).thenReturn(Optional.empty());

        User result = service.register(new GithubRegisterRequest(CODE, skills));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertSame(result, saved.getValue());
        assertEquals(EMAIL, result.getEmail().value());
        assertNull(result.getPassword());
        assertEquals(skills, result.getSkills());
    }

    @Test
    public void registerShouldThrowEmailAlreadyTakenWhenUserExists() {
        when(githubClient.fetchProfile(CODE)).thenReturn(new GithubProfile(EMAIL, NAME));
        when(userRepository.findByEmail(new Email(EMAIL))).thenReturn(Optional.of(new User(NAME, EMAIL, "hash", Set.of())));

        assertThrows(EmailAlreadyTakenException.class,
                () -> service.register(new GithubRegisterRequest(CODE, Set.of())));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void registerShouldPropagateGithubAuthenticationException() {
        when(githubClient.fetchProfile(CODE)).thenThrow(new GithubAuthenticationException("bad code"));

        assertThrows(GithubAuthenticationException.class,
                () -> service.register(new GithubRegisterRequest(CODE, Set.of())));
        verifyNoInteractions(userRepository);
    }
}
