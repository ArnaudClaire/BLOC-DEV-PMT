package com.mooc.formulaone.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Charge utile attendue pour une demande de connexion.
 *
 * @param email adresse email de l'utilisateur
 * @param password mot de passe en clair saisi côté client
 */
@Schema(name = "AuthLoginRequest", description = "Corps de requête utilisé pour authentifier un utilisateur.")
public record AuthLoginRequest(
        @Schema(description = "Adresse email utilisée pour la connexion.", example = "alice@pmt.fr")
        @NotBlank
        @Email
        String email,
        @Schema(description = "Mot de passe saisi côté client.", example = "MotDePasse123!")
        @NotBlank
        String password
) {
}
