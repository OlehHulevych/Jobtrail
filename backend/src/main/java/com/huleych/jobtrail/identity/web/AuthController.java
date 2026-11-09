package com.huleych.jobtrail.identity.web;

import com.huleych.jobtrail.identity.application.commands.RegisterUserCommand;
import com.huleych.jobtrail.identity.application.service.RegisterUser;
import com.huleych.jobtrail.identity.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RegisterUser registerUser;

    public AuthController(RegisterUser registerUser) {
        this.registerUser = registerUser;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        User user = registerUser.execute(new RegisterUserCommand(request.email(), request.password(), null));
        return new RegisterResponse(user.getId(), user.getEmail().value());
    }
}
