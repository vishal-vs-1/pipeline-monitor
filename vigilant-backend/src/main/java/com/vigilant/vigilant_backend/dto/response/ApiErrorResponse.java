package com.vigilant.vigilant_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
    int status,
    String message,
    List<String> errors,
    LocalDateTime timestamp
) {
    public ApiErrorResponse(int status, String message, List<String> errors) {
        this(status, message, errors, LocalDateTime.now());
    }
}
