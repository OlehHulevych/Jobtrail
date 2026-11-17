package com.huleych.jobtrail.identity.application.service;
import com.huleych.jobtrail.identity.application.commands.GoogleRegisterCommand;
import com.huleych.jobtrail.identity.domain.Email;
import com.huleych.jobtrail.identity.domain.User;
import com.huleych.jobtrail.identity.domain.UserRepository;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundByEmailException;
import com.huleych.jobtrail.identity.infrastructure.TokenService;
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

    public GoogleAuthService(@Qualifier("googleJwtDecoder") JwtDecoder googleJwtDecoder, UserRepository userRepository, TokenService tokenService){
        this.googleJwtDecoder = googleJwtDecoder;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
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
    public String login(String idToken){
        Jwt google = this.verify(idToken);
        String email = google.getClaimAsString("email");
        User user = userRepository.findByEmail(new Email(email)).orElse(null);
        if(user==null) throw new UserNotFoundByEmailException(email);
        return tokenService.createAccessToken(user);
    }

    @Transactional
    public User register(GoogleRegisterCommand command){
        Jwt google = this.verify(command.idToken());
        String email = google.getClaimAsString("email");
        User user = new User(email, null, command.skills());
        userRepository.save(user);
        return user;


    }
}
