package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de creation d'invitation de projet.
 *
 * @param email email du destinataire
 * @param role role propose
 * @param projectId identifiant du projet cible
 * @param invitedById identifiant de l'administrateur a l'origine de l'invitation
 */
@Schema(name = "ProjectInvitationCreateRequest", description = "Corps de requête utilisé pour inviter un membre par email.")
public record ProjectInvitationCreateRequest(
        @Schema(description = "Adresse email invitée.", example = "bob@pmt.fr")
        @NotBlank
        @Email
        String email,
        @Schema(description = "Rôle attribué après acceptation.", example = "MEMBER")
        @NotNull
        ProjectRole role,
        @Schema(description = "Identifiant du projet ciblé.", example = "12")
        @NotNull
        Long projectId,
        @Schema(description = "Identifiant de l'administrateur qui crée l'invitation.", example = "1")
        @NotNull
        Long invitedById
) {
}
