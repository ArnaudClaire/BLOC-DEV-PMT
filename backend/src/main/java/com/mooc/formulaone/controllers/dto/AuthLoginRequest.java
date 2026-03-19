package com.mooc.formulaone.controllers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Charge utile attendue pour une demande de connexion.
 *
 * @param email adresse email de l'utilisateur
 * @param password mot de passe en clair saisi côté client
 */
public record AuthLoginRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {
}
