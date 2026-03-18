package com.mooc.formulaone.controllers.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO d'acceptation d'une invitation pour un utilisateur authentifie cote front.
 *
 * @param userId identifiant de l'utilisateur qui accepte l'invitation
 */
public record ProjectInvitationAcceptRequest(
        @NotNull
        Long userId
) {
}
