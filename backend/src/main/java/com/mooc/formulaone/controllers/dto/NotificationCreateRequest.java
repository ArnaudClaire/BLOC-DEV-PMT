package com.mooc.formulaone.controllers.dto;

import com.mooc.formulaone.models.NotificationStatus;
import com.mooc.formulaone.models.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * DTO de creation de notification.
 *
 * @param type type de notification
 * @param status statut initial
 * @param message message fonctionnel
 * @param sentAt date d'envoi
 * @param userId identifiant du destinataire
 * @param taskId identifiant optionnel de la tache rattachee
 */
public record NotificationCreateRequest(
        @NotNull
        NotificationType type,
        @NotNull
        NotificationStatus status,
        @NotBlank
        String message,
        Instant sentAt,
        @NotNull
        Long userId,
        Long taskId
) {
}
