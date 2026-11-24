package com.huleych.jobtrail.identity.application.service;
import com.huleych.jobtrail.identity.application.result.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GoogleRegisterRequest;
import com.huleych.jobtrail.identity.domain.valueObjects.Email;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.infrastructure.repository.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.EmailAlreadyTakenException;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundByEmailException;
import com.huleych.jobtrail.identity.infrastructure.service.TokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GoogleAuthService {
    private final JwtDecoder googleJwtDecoder;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public GoogleAuthService(@Qualifier("googleJwtDecoder") JwtDecoder googleJwtDecoder, UserRepository userRepository, TokenService tokenService, RefreshTokenService refreshTokenService){
        this.googleJwtDecoder = googleJwtDecoder;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    public Jwt verify(String idToken){
        Jwt google;
        try {
            google = googleJwtDecoder.decode(idToken);
        }
        catch (JwtException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid google token");
        }
        if(!Boolean.TRUE.equals(google.getClaimAsBoolean("email_verified"))){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email is not verified");
        }
        return google;
    }

    @Transactional
    public LoginResult login(String idToken){
        Jwt google = this.verify(idToken);
        String email = google.getClaimAsString("email");
        User user = userRepository.findByEmail(new Email(email)).orElse(null);
        if(user==null) throw new UserNotFoundByEmailException(email);
        return new LoginResult(tokenService.createAccessToken(user),refreshTokenService.createRefreshToken(user));
    }

    @Transactional
    public User register(GoogleRegisterRequest command){
        Jwt google = this.verify(command.idToken());
        String email = google.getClaimAsString("email");
        User user = userRepository.findByEmail(new Email(email)).orElse(null);
        if(user!=null) throw new EmailAlreadyTakenException(email);
        String name = google.getClaimAsString("name");
        User newUser = new User(name, email, null, command.skills());
        userRepository.save(newUser);
        return newUser;


    }
}
