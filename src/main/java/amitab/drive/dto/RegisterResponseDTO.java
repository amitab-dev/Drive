package amitab.drive.dto;

import amitab.drive.entity.UserRole;
import amitab.drive.entity.User;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponseDTO(
        UUID id,
        String fullName,
        String email,
        String avatar,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
    public static RegisterResponseDTO from(User user) {
        return new RegisterResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
