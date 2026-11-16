package amitab.drive.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ObjectStorage {
    String uploadFile(MultipartFile file, UUID userId) throws IOException;

    String uploadFolder(List<MultipartFile> file, UUID userId) throws  IOException;

    String createFolder(String name, UUID userId);

    byte[] downloadFile(String key);

    List<?> listFiles(UUID userId);

    void deleteFile(String key);
}
