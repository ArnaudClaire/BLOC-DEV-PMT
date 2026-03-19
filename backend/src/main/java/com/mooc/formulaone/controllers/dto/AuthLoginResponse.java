package com.mooc.formulaone.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Réponse simplifiée renvoyée au front après une connexion réussie.
 *
 * @param id identifiant utilisateur
 * @param username pseudo affiché dans l'interface
 * @param email adresse email de référence
 */
@Schema(name = "AuthLoginResponse", description = "Informations minimales renvoyées au frontend après une connexion réussie.")
public record AuthLoginResponse(
        @Schema(description = "Identifiant technique de l'utilisateur.", example = "1")
        Long id,
        @Schema(description = "Nom d'affichage de l'utilisateur.", example = "Alice Martin")
        String username,
        @Schema(description = "Adresse email de référence.", example = "alice@pmt.fr")
        String email
) {
}
