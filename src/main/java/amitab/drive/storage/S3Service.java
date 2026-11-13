package amitab.drive.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
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
        String key = userId.toString() + "/" + fileId;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return key;
    }

    @Override
    public byte[] downloadFile(String key) {
        return new byte[0];
    }

    @Override
    public void deleteFile(String key) {
        s3Client.deleteObject(b -> b.bucket(bucket).key(key));
    }
}
