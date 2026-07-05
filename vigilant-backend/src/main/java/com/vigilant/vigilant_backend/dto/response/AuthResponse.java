package com.vigilant.vigilant_backend.dto.response;

public record AuthResponse(String accessToken, String refreshToken, UserDto userDto) {}
