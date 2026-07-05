package com.vigilant.vigilant_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "{validation.token.required}") String refreshToken
) {}
