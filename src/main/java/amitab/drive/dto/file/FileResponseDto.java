package amitab.drive.dto.file;

import amitab.drive.entity.File;

import java.util.UUID;

public record FileResponseDto(
        boolean success,
        UUID id,
        String key
) {
    public static FileResponseDto from(File file) {
        return new FileResponseDto(true, file.getId(), file.getFileKey());
    }

    public static FileResponseDto from(String key) {
        return new FileResponseDto(true, null, key);
    }
}
