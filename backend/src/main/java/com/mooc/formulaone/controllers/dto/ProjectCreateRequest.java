package com.mooc.formulaone.controllers.dto;

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
public record ProjectCreateRequest(
        @NotBlank
        String name,
        String description,
        LocalDate startDate,
        @NotNull
        Long ownerId
) {
}
