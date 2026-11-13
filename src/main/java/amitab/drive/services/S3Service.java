package amitab.drive.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;

@RequiredArgsConstructor
@Service
public class S3Service {

    @Value("${aws.s3.access-key}")
    private final String accessKey;

    @Value("${aws.s3.secret-key}")
    private final String secretKey;

    @Getter
    @Value("${aws.s3.bucket}")
    private final String bucket;

    @Bean
    private S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    public void uploadFile(String filename, InputStream inputStream)
            throws S3Exception, AwsServiceException, SdkClientException, IOException {
        S3Client client = getS3Client();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .build();
        client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));
    }

}
