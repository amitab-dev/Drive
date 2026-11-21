package amitab.drive.dto.file;

import amitab.drive.entity.File;

import java.time.Instant;
import java.util.UUID;

public record FileItemDto(
        UUID id,
        String key,
        String name,
        long size,
        Instant modifiedAt
) {
    public static FileItemDto from(File f) {
        return new FileItemDto(
                f.getId(),
                f.getFileKey(),
                f.getFileName(),
                f.getFileSize(),
                f.getUpdatedAt()
        );
    }
}
