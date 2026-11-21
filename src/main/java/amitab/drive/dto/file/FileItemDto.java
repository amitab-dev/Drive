package amitab.drive.dto.file;

import java.time.Instant;
import java.util.UUID;

public record FileItemDto(
        UUID id,
        String key,
        String name,
        long size,
        Instant modifiedAt
) {
}
