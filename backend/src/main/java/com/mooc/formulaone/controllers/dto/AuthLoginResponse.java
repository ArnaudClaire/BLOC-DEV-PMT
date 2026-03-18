package com.mooc.formulaone.controllers.dto;

public record AuthLoginResponse(
        Long id,
        String username,
        String email
) {
}
