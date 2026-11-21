package amitab.drive.storage;

import amitab.drive.dto.file.FileItemDto;
import amitab.drive.exception.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    public void uploadFile(MultipartFile file, String key) throws IOException {
//        String fileId = UUID.randomUUID().toString();
//        String key = userId + "/" + fileId + "::" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    @Override
    public String uploadFolder(List<MultipartFile> files, UUID userId) throws IOException {
        return "";
    }

    @Override
    public String createFolder(String folderName, UUID userId) {
        String key = userId.toString() + "/" + folderName + "/";

        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(key).build();

        s3Client.putObject(request, RequestBody.empty());
        return key;
    }

    @Override
    public byte[] downloadFile(String key) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    @Override
    public void downloadLargeFile(String key, OutputStream outputStream) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .key(key)
                .bucket(bucket)
                .build();
        ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request);
        inputStream.transferTo(outputStream);
    }

    @Override
    public void deleteFile(String key) {
        s3Client.deleteObject(b -> b.bucket(bucket).key(key));
    }

    @Override
    public List<?> listFolders(UUID userId) {
        return List.of("");
    }
}
