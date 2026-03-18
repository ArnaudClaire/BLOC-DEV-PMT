package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

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
