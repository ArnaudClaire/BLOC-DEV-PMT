package com.mooc.formulaone.controllers.dto;

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
public record UserCreateRequest(
        @NotBlank
        @Size(max = 100)
        String username,
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        @NotBlank
        @Size(min = 8, max = 255)
        String password
) {
}
