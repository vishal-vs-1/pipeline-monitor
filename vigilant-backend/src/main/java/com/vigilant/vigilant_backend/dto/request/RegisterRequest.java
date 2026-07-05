package com.vigilant.vigilant_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "{validation.name.required}") String name,
    @NotBlank(message = "{validation.email.required}") @Email(message = "{validation.email.invalid}") String email,
    @NotBlank(message = "{validation.password.required}") @Size(min = 6, message = "{validation.password.size}") String password
) {}
