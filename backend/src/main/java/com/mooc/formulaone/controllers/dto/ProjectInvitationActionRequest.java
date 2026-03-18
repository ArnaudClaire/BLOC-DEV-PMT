package com.mooc.formulaone.controllers.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO simple pour les actions admin sur une invitation.
 *
 * @param requestedById identifiant de l'administrateur demandeur
 */
public record ProjectInvitationActionRequest(
        @NotNull
        Long requestedById
) {
}
