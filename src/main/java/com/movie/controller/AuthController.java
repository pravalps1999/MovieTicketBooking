package com.movie.controller;

import com.movie.domain.User;
import com.movie.resource.LoginResource;
import com.movie.service.UserAuthService;
import com.movie.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserAuthService userAuthService;

    @PostMapping("/login")
    public String login(@RequestBody LoginResource resource) {

        // 1. Authenticate user
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        resource.getEmail(),
                        resource.getPassword()
                );

        authenticationManager.authenticate(authentication);

        // 2. Load user from DB
        User user = (User) userAuthService.loadUserByUsername(resource.getEmail());

        // 3. Generate JWT with role
        return jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );
    }
}
