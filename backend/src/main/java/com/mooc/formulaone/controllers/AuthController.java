package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.AuthLoginRequest;
import com.mooc.formulaone.controllers.dto.AuthLoginResponse;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Authentification",
        description = "Endpoints de connexion permettant de vérifier les identifiants et d'ouvrir une session côté frontend."
)
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
    @Operation(
            summary = "Connecter un utilisateur",
            description = "Vérifie l'adresse email et le mot de passe transmis par le client puis retourne les informations de session minimales à conserver côté frontend."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Identifiants de connexion de l'utilisateur.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Connexion standard",
                    value = """
                            {
                              "email": "alice@pmt.fr",
                              "password": "MotDePasse123!"
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie."),
            @ApiResponse(responseCode = "400", description = "Charge utile invalide."),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides.")
    })
    public AuthLoginResponse login(@Valid @RequestBody AuthLoginRequest request) {
        User user = userService.authenticate(request.email(), request.password());
        return new AuthLoginResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
