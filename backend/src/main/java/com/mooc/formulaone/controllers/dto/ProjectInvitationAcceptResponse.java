package com.mooc.formulaone.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Reponse retournee apres l'acceptation d'une invitation.
 *
 * @param projectId identifiant du projet rejoint
 */
@Schema(name = "ProjectInvitationAcceptResponse", description = "Réponse renvoyée après acceptation réussie d'une invitation.")
public record ProjectInvitationAcceptResponse(
        @Schema(description = "Identifiant du projet rejoint par l'utilisateur.", example = "12")
        Long projectId
) {
}
