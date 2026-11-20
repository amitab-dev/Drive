package amitab.drive.storage;

import amitab.drive.dto.file.FileItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service implements ObjectStorage {

    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public String uploadFile(MultipartFile file, UUID userId) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String key = userId.toString() + "/" + fileId + "::" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return key;
    }

    @Override
    public String uploadFolder(List<MultipartFile> files, UUID userId) throws IOException {
        return "";
    }

    @Override
    public String createFolder(String folderName, UUID userId) {
        String key = userId.toString() + "/" + folderName + "/";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.putObject(request, RequestBody.empty());
        return key;
    }

    @Override
    public byte[] downloadFile(String key) {
        return new byte[0];
    }

    @Override
    public List<FileItemDto> listFiles(UUID userId) {
        String prefix = userId + "/";

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        List<FileItemDto> files = new ArrayList<>();
        for (ListObjectsV2Response pages: s3Client.listObjectsV2Paginator(request)) {
            for (S3Object objs : pages.contents()) {
                if (objs.key().endsWith("/")) continue;
                String name  = objs.key().substring(prefix.length());
                files.add(new FileItemDto(objs.key(), name, objs.size(), objs.lastModified()));
            }

        }
        return files;
    }

    @Override
    public void deleteFile(String key) {
        s3Client.deleteObject(b -> b.bucket(bucket).key(key));
    }
}
