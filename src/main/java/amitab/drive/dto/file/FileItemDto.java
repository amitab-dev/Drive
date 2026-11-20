package amitab.drive.dto.file;

import java.time.Instant;

public record FileItemDto(
        String key,
        String name,
        long size,
        Instant modifiedAt
) {
}
