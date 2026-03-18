package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

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
