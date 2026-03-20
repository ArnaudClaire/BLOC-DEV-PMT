package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de mise a jour de role pour un membre de projet.
 *
 * @param role nouveau role attribue au membre
 * @param requestedById identifiant de l'administrateur qui realise l'action
 */
@Schema(name = "ProjectMemberUpdateRequest", description = "Corps de requete utilise pour modifier le role d'un membre de projet.")
public record ProjectMemberUpdateRequest(
        @Schema(description = "Nouveau role du membre.", example = "OBSERVER")
        @NotNull
        ProjectRole role,
        @Schema(description = "Identifiant de l'administrateur qui effectue le changement.", example = "1")
        @NotNull
        Long requestedById
) {
}
