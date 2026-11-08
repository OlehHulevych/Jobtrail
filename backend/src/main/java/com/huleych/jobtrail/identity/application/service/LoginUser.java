package com.huleych.jobtrail.identity.application.service;
import com.huleych.jobtrail.identity.application.LoginResult;
import com.huleych.jobtrail.identity.application.commands.LoginCommand;
import com.huleych.jobtrail.identity.domain.Email;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.domain.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.InvalidCredentialsException;
import com.huleych.jobtrail.identity.infrastructure.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class LoginUser {
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUser(TokenService tokenService, RefreshTokenService refreshTokenService, UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResult execute(LoginCommand command){
        if(command == null) throw new IllegalArgumentException("Request body is required for login");
        Email email;
        try {
            email = new Email(command.email());
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException();
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if(user==null) throw new InvalidCredentialsException();
        if(!passwordEncoder.matches(command.password(),user.getPasswordHash())) throw new InvalidCredentialsException();
        String jwtToken = tokenService.createAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new LoginResult(jwtToken,refreshToken);



    }


}
