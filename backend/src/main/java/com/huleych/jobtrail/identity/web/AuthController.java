package com.huleych.jobtrail.identity.web;

import com.huleych.jobtrail.identity.application.LoginResult;
import com.huleych.jobtrail.identity.application.commands.LoginCommand;
import com.huleych.jobtrail.identity.application.commands.LogoutCommand;
import com.huleych.jobtrail.identity.application.commands.RefreshCommand;
import com.huleych.jobtrail.identity.application.commands.RegisterUserCommand;
import com.huleych.jobtrail.identity.application.service.LoginUser;
import com.huleych.jobtrail.identity.application.service.LogoutUser;
import com.huleych.jobtrail.identity.application.service.RefreshAccessToken;
import com.huleych.jobtrail.identity.application.service.RegisterUser;
import com.huleych.jobtrail.identity.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RegisterUser registerUser;
    private final LoginUser loginUser;
    private final LogoutUser logoutUser;
    private final RefreshAccessToken refreshAccessToken;


    public AuthController(RegisterUser registerUser, LoginUser loginUser, RefreshAccessToken refreshAccessToken, LogoutUser logoutUser) {
        this.registerUser = registerUser;
        this.loginUser = loginUser;
        this.refreshAccessToken = refreshAccessToken;
        this.logoutUser = logoutUser;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        User user = registerUser.execute(new RegisterUserCommand(request.email(), request.password(), null));
        return new RegisterResponse(user.getId(), user.getEmail().value());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        LoginResult result = loginUser.execute(new LoginCommand(request.email(), request.password()));
        return new LoginResponse(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody RefreshRequest request) {
        LoginResult result = refreshAccessToken.execute(new RefreshCommand(request.refreshToken()));
        return new LoginResponse(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody RefreshRequest request){
        logoutUser.execute(new LogoutCommand(request.refreshToken()));
    }

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal User user){
        return String.valueOf(user.getId());
    }
}
