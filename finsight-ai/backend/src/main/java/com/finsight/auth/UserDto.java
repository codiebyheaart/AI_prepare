package com.finsight.auth;

import java.time.LocalDateTime;

public record UserDto(Long id, String email, String fullName, String role, LocalDateTime createdAt) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getCreatedAt());
    }
}
