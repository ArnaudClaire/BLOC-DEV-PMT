package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.AuthLoginRequest;
import com.mooc.formulaone.controllers.dto.AuthLoginResponse;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
/**
 * Gère les opérations de connexion utilisateur côté API.
 */
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Vérifie l'email et le mot de passe puis retourne l'utilisateur connecté.
     *
     * @param request identifiants saisis par le client
     * @return les informations minimales de session côté front
     */
    @PostMapping("/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthLoginResponse login(@Valid @RequestBody AuthLoginRequest request) {
        User user = userService.authenticate(request.email(), request.password());
        return new AuthLoginResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
