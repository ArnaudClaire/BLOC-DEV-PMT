package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Charge utile de création d'un membre de projet.
 *
 * @param role rôle attribué dans le projet
 * @param joinedAt date d'entrée facultative
 * @param projectId projet cible
 * @param userId utilisateur à rattacher
 */
public record ProjectMemberCreateRequest(
        @NotNull
        ProjectRole role,
        Instant joinedAt,
        @NotNull
        Long projectId,
        @NotNull
        Long userId
) {
}
