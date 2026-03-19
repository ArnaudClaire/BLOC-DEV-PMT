package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "TaskCreateRequest", description = "Corps de requête utilisé pour créer une tâche.")
public record TaskCreateRequest(
        @Schema(description = "Titre court de la tâche.", example = "Préparer la démonstration")
        @NotBlank
        String title,
        @Schema(description = "Description détaillée de la tâche.", example = "Assembler les captures et les scénarios de présentation.")
        String description,
        @Schema(description = "Statut initial de la tâche, correspondant à une colonne du projet.", example = "TODO")
        @NotBlank
        String status,
        @Schema(description = "Priorité métier de la tâche.", example = "HIGH")
        @NotNull
        TaskPriority priority,
        @Schema(description = "Date cible de livraison.", example = "2026-03-25", nullable = true)
        LocalDate dueDate,
        @Schema(description = "Date de fin effective ou prévisionnelle.", example = "2026-03-25", nullable = true)
        LocalDate endDate,
        @Schema(description = "Identifiant du projet rattaché.", example = "12")
        @NotNull
        Long projectId,
        @Schema(description = "Identifiant du créateur de la tâche.", example = "1")
        @NotNull
        Long createdById,
        @Schema(description = "Identifiant du collaborateur assigné.", example = "4", nullable = true)
        Long assignedToId
) {
}
