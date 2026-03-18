package com.mooc.formulaone.controllers.dto;

/**
 * Reponse retournee apres l'acceptation d'une invitation.
 *
 * @param projectId identifiant du projet rejoint
 */
public record ProjectInvitationAcceptResponse(
        Long projectId
) {
}
