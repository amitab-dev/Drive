package amitab.drive.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface ObjectStorage {
    String uploadFile(MultipartFile file, UUID userId) throws IOException;

    byte[] downloadFile(String key);

    void deleteFile(String key);
}
