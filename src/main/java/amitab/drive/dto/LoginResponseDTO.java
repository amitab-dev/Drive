package amitab.drive.dto;

import java.util.UUID;

public record LoginResponseDTO(
        UUID userId,
        String accessToken
) {
}
