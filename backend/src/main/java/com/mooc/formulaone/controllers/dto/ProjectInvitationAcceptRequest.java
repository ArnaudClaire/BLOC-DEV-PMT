package com.mooc.formulaone.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO d'acceptation d'une invitation pour un utilisateur authentifie cote front.
 *
 * @param userId identifiant de l'utilisateur qui accepte l'invitation
 */
@Schema(name = "ProjectInvitationAcceptRequest", description = "Corps de requête utilisé pour accepter une invitation par token.")
public record ProjectInvitationAcceptRequest(
        @Schema(description = "Identifiant de l'utilisateur connecté qui accepte l'invitation.", example = "4")
        @NotNull
        Long userId
) {
}
