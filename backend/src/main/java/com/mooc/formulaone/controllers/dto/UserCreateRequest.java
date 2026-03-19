package com.mooc.formulaone.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de creation d'utilisateur expose par l'API REST.
 * Il porte les validations minimales avant mapping vers l'entite.
 *
 * @param username nom d'affichage de l'utilisateur
 * @param email adresse email unique attendue
 * @param password mot de passe brut a encoder au niveau service
 */
@Schema(name = "UserCreateRequest", description = "Corps de requête utilisé pour créer un utilisateur.")
public record UserCreateRequest(
        @Schema(description = "Nom d'affichage de l'utilisateur.", example = "Alice Martin")
        @NotBlank
        @Size(max = 100)
        String username,
        @Schema(description = "Adresse email unique de l'utilisateur.", example = "alice@pmt.fr")
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        @Schema(description = "Mot de passe brut envoyé au backend pour encodage.", example = "MotDePasse123!")
        @NotBlank
        @Size(min = 8, max = 255)
        String password
) {
}
