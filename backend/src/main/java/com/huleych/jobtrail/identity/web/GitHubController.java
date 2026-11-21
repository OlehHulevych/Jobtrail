package com.huleych.jobtrail.identity.web;

import com.huleych.jobtrail.identity.application.LoginResult;
import com.huleych.jobtrail.identity.application.commands.GithubLoginRequest;
import com.huleych.jobtrail.identity.application.commands.GithubRegisterRequest;
import com.huleych.jobtrail.identity.application.commands.RegisterUserCommand;
import com.huleych.jobtrail.identity.application.service.GithubAuthService;
import com.huleych.jobtrail.identity.domain.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/github")
public class GitHubController {
    private final GithubAuthService githubAuthService;

    public GitHubController(GithubAuthService githubAuthService){
        this.githubAuthService = githubAuthService;
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody GithubRegisterRequest request) {
        User user = githubAuthService.register(request);
        return new RegisterResponse(user.getId(), user.getName().value(), user.getEmail().value());
    }

    @PostMapping("/login")
    public LoginResult login(@Valid @RequestBody GithubLoginRequest request){
        return githubAuthService.login(request.code());
    }
}
