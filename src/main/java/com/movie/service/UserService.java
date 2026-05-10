package com.movie.service;

import com.movie.Repository.UserRepository;
import com.movie.domain.User;
import com.movie.exception.ResourceNotFoundException;
import com.movie.resource.UserResource;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    public UserResource addUser(UserResource userResource) {
        User user=User.toEntity(userResource);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saveuser=userRepository.save(user);
        return User.toResource(saveuser);
    }

    public UserResource getUser(Long id) {
        User user=userRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("User not found with id: " + id));
        return User.toResource(user);
    }

    public String verify(String username, String password) {
        Authentication authentication =authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        if(authentication.isAuthenticated()){
            return "Login Successfully";
        }
        return "Login Failed";
    }
}
