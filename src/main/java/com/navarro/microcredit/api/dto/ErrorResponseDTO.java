package com.navarro.microcredit.api.dto;

public record ErrorResponseDTO(
        String error,
        String message
) { }
