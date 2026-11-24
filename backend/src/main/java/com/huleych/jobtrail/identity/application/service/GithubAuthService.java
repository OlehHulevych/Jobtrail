package com.huleych.jobtrail.identity.application.service;

import com.huleych.jobtrail.identity.application.result.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GithubRegisterRequest;
import com.huleych.jobtrail.identity.domain.valueObjects.Email;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.infrastructure.repository.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundByEmailException;
import com.huleych.jobtrail.identity.infrastructure.client.GithubClient;
import com.huleych.jobtrail.identity.infrastructure.service.TokenService;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class GithubAuthService {
    private final GithubClient githubClient;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public GithubAuthService(GithubClient githubClient, UserRepository userRepository, TokenService tokenService, RefreshTokenService refreshTokenService){
        this.githubClient = githubClient;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public User register(GithubRegisterRequest request){
        var profile = githubClient.fetchProfile(request.code());
        var email = profile.email();
        if(userRepository.findByEmail(new Email(email)).isPresent()) throw new EmailAlreadyTakenException(email);
        User newUser = new User(profile.name(),email,null,request.skills());
        userRepository.save(newUser);
        return newUser;
    }

    @Transactional
    public LoginResult login(String code){
        var email = githubClient.fetchProfile(code).email();
        User user = userRepository.findByEmail(new Email(email)).orElse(null);
        if(user==null) throw new UserNotFoundByEmailException(email);
        return new LoginResult(tokenService.createAccessToken(user),refreshTokenService.createRefreshToken(user));
    }

}
