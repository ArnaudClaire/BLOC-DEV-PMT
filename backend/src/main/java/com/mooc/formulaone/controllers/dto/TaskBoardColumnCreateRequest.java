package com.mooc.formulaone.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de creation d'une colonne de board de taches.
 *
 * @param name libelle de la colonne
 * @param displayOrder ordre d'affichage souhaite
 * @param projectId projet rattache
 */
public record TaskBoardColumnCreateRequest(
        @NotBlank
        String name,
        Integer displayOrder,
        @NotNull
        Long projectId
) {
}
