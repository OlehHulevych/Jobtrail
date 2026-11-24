package com.huleych.jobtrail.identity.web;

import com.huleych.jobtrail.identity.application.result.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GoogleRegisterRequest;
import com.huleych.jobtrail.identity.application.service.GoogleAuthService;
import com.huleych.jobtrail.identity.domain.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/google")
@RequiredArgsConstructor
public class GoogleController {
    private final GoogleAuthService googleAuthService;

    public record GoogleLoginRequest(@NotBlank String idToken){}
    public record TokenResponse (String accessToken){}

    @PostMapping("/login")
    public LoginResult googleLogin(@Valid @RequestBody GoogleLoginRequest req) {
        return googleAuthService.login(req.idToken());
    }

    @PostMapping("/register")
    public RegisterResponse googleRequest(@Valid @RequestBody GoogleRegisterRequest req){
        User user = googleAuthService.register(req);
        return new RegisterResponse(user.getId(), user.getName().value(), user.getEmail().value());
    }

}
