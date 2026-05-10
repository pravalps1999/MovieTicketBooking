package com.movie.controller;

import com.movie.resource.UserResource;
import com.movie.service.UserService;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/test")
    public String userApi() {
        return "User access";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/add-movie")
    public String addMovie() {
        return "Admin only";
    }

    @PostMapping("/add")
    public ResponseEntity<UserResource> addUser(@RequestBody UserResource userResource){
        return ResponseEntity.ok(userService.addUser(userResource));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                        @RequestParam String password) {
        String result = userService.verify(email, password);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResource> getUser(@PathVariable(name = "id") @Min(value = 1, message = "Id must be greater than or equal to 1") Long id){
        return ResponseEntity.ok(userService.getUser(id));
    }
}
