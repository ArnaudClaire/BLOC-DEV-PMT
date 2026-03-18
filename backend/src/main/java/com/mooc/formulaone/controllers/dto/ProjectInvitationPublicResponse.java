package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.InvitationStatus;
import com.mooc.formulaone.models.ProjectRole;

import java.sql.Timestamp;

/**
 * Vue publique minimale d'une invitation resolue a partir d'un token.
 *
 * @param id identifiant technique
 * @param email email invite
 * @param role role attribue a l'acceptation
 * @param status statut courant
 * @param projectId projet cible
 * @param projectName nom du projet cible
 * @param expiresAt date d'expiration
 */
public record ProjectInvitationPublicResponse(
        Long id,
        String email,
        ProjectRole role,
        InvitationStatus status,
        Long projectId,
        String projectName,
        Timestamp expiresAt
) {
}
