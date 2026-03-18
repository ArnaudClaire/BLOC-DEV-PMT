package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.AuthLoginRequest;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public User login(@Valid @RequestBody AuthLoginRequest request) {
        return userService.authenticate(request.email(), request.password());
    }
}
