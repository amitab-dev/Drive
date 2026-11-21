package amitab.drive.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ObjectStorage {
    String uploadFile(MultipartFile file, UUID userId) throws IOException;

    String uploadFolder(List<MultipartFile> file, UUID userId) throws  IOException;

    String createFolder(String name, UUID userId);

    InputStream downloadFile(String key) throws IOException;

    List<?> listFiles(UUID userId);

    void deleteFile(String key);

    List<?> listFolders(UUID userId);
}
