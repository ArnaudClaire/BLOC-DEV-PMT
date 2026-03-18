package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.TaskPriority;
import com.mooc.formulaone.models.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO de creation de tache utilise par le controller REST.
 * Il decrit les champs fonctionnels attendus ainsi que les identifiants
 * necessaires pour reconstruire les relations metier.
 *
 * @param title titre court de la tache
 * @param description description detaillee optionnelle
 * @param status statut initial attendu
 * @param priority priorite de traitement
 * @param dueDate date cible de livraison
 * @param endDate date de fin effective ou prevue
 * @param projectId identifiant du projet rattache
 * @param createdById identifiant du createur
 * @param assignedToId identifiant du collaborateur assigne
 */
public record TaskCreateRequest(
        @NotBlank
        String title,
        String description,
        @NotNull
        TaskStatus status,
        @NotNull
        TaskPriority priority,
        LocalDate dueDate,
        LocalDate endDate,
        @NotNull
        Long projectId,
        @NotNull
        Long createdById,
        Long assignedToId
) {
}
