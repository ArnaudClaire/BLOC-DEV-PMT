package com.mooc.formulaone.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO de creation de projet expose par l'API REST.
 *
 * @param name nom du projet
 * @param description description fonctionnelle
 * @param startDate date de demarrage
 * @param ownerId identifiant du proprietaire
 */
@Schema(name = "ProjectCreateRequest", description = "Corps de requête utilisé pour créer un projet.")
public record ProjectCreateRequest(
        @Schema(description = "Nom métier du projet.", example = "Projet PMT")
        @NotBlank
        String name,
        @Schema(description = "Description fonctionnelle du projet.", example = "Outil de gestion de projet partagé.")
        String description,
        @Schema(description = "Date de démarrage du projet.", example = "2026-03-19")
        LocalDate startDate,
        @Schema(description = "Identifiant du propriétaire du projet.", example = "1")
        @NotNull
        Long ownerId
) {
}
