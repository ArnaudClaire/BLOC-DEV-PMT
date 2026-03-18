package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.ProjectRole;
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
public record ProjectInvitationCreateRequest(
        @NotBlank
        @Email
        String email,
        @NotNull
        ProjectRole role,
        @NotNull
        Long projectId,
        @NotNull
        Long invitedById
) {
}
