package amitab.drive.dto.file;

public record FileResponseDto(
        boolean success,
        String key
) {
    public static FileResponseDto from(String key) {
        return new FileResponseDto(true, key);
    }
}
