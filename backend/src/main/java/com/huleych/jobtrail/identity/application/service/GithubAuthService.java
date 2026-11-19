package com.huleych.jobtrail.identity.application.service;

import com.huleych.jobtrail.identity.application.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GithubRegisterRequest;
import com.huleych.jobtrail.identity.domain.Email;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.domain.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundByEmailException;
import com.huleych.jobtrail.identity.infrastructure.GithubClient;
import com.huleych.jobtrail.identity.infrastructure.TokenService;
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
        var email = githubClient.fetchVerifiedEmail(request.code());
        if(userRepository.findByEmail(new Email(email)).isPresent()) throw new EmailAlreadyTakenException(email);
        User newUser = new User(email,null,request.skills());
        userRepository.save(newUser);
        return newUser;
    }

    @Transactional
    public LoginResult login(String code){
        var email = githubClient.fetchVerifiedEmail(code);
        User user = userRepository.findByEmail(new Email(email)).orElse(null);
        if(user==null) throw new UserNotFoundByEmailException(email);
        return new LoginResult(tokenService.createAccessToken(user),refreshTokenService.createRefreshToken(user));
    }

}
