package com.mooc.formulaone.controllers.dto;

/**
 * Réponse simplifiée renvoyée au front après une connexion réussie.
 *
 * @param id identifiant utilisateur
 * @param username pseudo affiché dans l'interface
 * @param email adresse email de référence
 */
public record AuthLoginResponse(
        Long id,
        String username,
        String email
) {
}
