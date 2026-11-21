package amitab.drive.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

public interface ObjectStorage {
    void uploadFile(MultipartFile file, String key) throws IOException;

    String uploadFolder(List<MultipartFile> file, UUID userId) throws IOException;

    String createFolder(String name, UUID userId);

    byte[] downloadFile(String key) throws IOException;

    void downloadLargeFile(String key, OutputStream outputStream) throws IOException;

    void deleteFile(String key);

    List<?> listFolders(UUID userId);
}
