package amitab.drive.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ObjectStorage {
    String uploadFile(MultipartFile file, UUID user) throws IOException;

    String createFolder(String name, UUID userId);

    InputStream openStream(String key) throws IOException;

    void deleteFile(String key);
}