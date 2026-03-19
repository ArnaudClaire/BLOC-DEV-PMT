package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Charge utile de mise à jour d'une tâche existante.
 *
 * @param title titre mis à jour
 * @param description description détaillée
 * @param status nouveau statut
 * @param priority nouvelle priorité
 * @param dueDate nouvelle échéance
 * @param endDate date de fin optionnelle
 * @param assignedToId utilisateur assigné optionnel
 */
@Schema(name = "TaskUpdateRequest", description = "Corps de requête utilisé pour modifier une tâche existante.")
public record TaskUpdateRequest(
        @Schema(description = "Titre mis à jour.", example = "Préparer la démonstration")
        @NotBlank
        String title,
        @Schema(description = "Description mise à jour.", example = "Scénario finalisé et relu.")
        String description,
        @Schema(description = "Nouveau statut de la tâche.", example = "IN_PROGRESS")
        @NotBlank
        String status,
        @Schema(description = "Nouvelle priorité métier.", example = "HIGH")
        @NotNull
        TaskPriority priority,
        @Schema(description = "Nouvelle date d'échéance.", example = "2026-03-25", nullable = true)
        LocalDate dueDate,
        @Schema(description = "Date de fin effective ou prévue.", example = "2026-03-24", nullable = true)
        LocalDate endDate,
        @Schema(description = "Identifiant du collaborateur assigné.", example = "4", nullable = true)
        Long assignedToId
) {
}
