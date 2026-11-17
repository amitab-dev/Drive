package amitab.drive.dto;

import java.util.UUID;

public record TokenPair(
        UUID userId,
        String accessToken,
        String refreshToken
) {
}
