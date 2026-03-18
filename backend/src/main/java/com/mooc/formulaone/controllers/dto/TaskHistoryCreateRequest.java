package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.TaskHistoryAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de creation d'historique de tache.
 *
 * @param actionType type de changement
 * @param fieldName champ modifie
 * @param oldValue ancienne valeur
 * @param newValue nouvelle valeur
 * @param taskId identifiant de la tache
 * @param changedById identifiant de l'utilisateur auteur du changement
 */
public record TaskHistoryCreateRequest(
        @NotNull
        TaskHistoryAction actionType,
        @NotBlank
        String fieldName,
        String oldValue,
        String newValue,
        @NotNull
        Long taskId,
        @NotNull
        Long changedById
) {
}
