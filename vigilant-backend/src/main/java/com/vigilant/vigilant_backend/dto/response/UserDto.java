package com.vigilant.vigilant_backend.dto.response;

import com.vigilant.vigilant_backend.entity.User;

public record UserDto(Long id, String email, String name) {
    public static UserDto fromEntity(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }
}
