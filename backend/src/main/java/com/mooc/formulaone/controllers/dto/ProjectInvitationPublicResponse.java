package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.InvitationStatus;
import com.mooc.formulaone.models.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(name = "ProjectInvitationPublicResponse", description = "Vue publique d'une invitation exposée à partir d'un token.")
public record ProjectInvitationPublicResponse(
        @Schema(description = "Identifiant interne de l'invitation.", example = "8")
        Long id,
        @Schema(description = "Adresse email destinataire de l'invitation.", example = "bob@pmt.fr")
        String email,
        @Schema(description = "Rôle accordé après acceptation.", example = "MEMBER")
        ProjectRole role,
        @Schema(description = "Statut courant de l'invitation.", example = "PENDING")
        InvitationStatus status,
        @Schema(description = "Identifiant du projet ciblé.", example = "12")
        Long projectId,
        @Schema(description = "Nom du projet ciblé.", example = "Projet PMT")
        String projectName,
        @Schema(description = "Date d'expiration de l'invitation.", example = "2026-03-26T10:00:00.000+00:00", nullable = true)
        Timestamp expiresAt
) {
}
