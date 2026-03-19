package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.TaskPriority;
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
public record TaskUpdateRequest(
        @NotBlank
        String title,
        String description,
        @NotBlank
        String status,
        @NotNull
        TaskPriority priority,
        LocalDate dueDate,
        LocalDate endDate,
        Long assignedToId
) {
}
