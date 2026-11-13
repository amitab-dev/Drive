package amitab.drive.services;

import amitab.drive.dto.MessageResponseDTO;
import amitab.drive.entity.File;
import amitab.drive.entity.FileStatus;
import amitab.drive.entity.FileType;
import amitab.drive.entity.User;
import amitab.drive.exception.BadRequestException;
import amitab.drive.exception.InternalServerErrorException;
import amitab.drive.exception.NotFoundException;
import amitab.drive.repository.FileRepository;
import amitab.drive.repository.UserRepository;
import amitab.drive.storage.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Service s3Service;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponseDTO uploadFile(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file found, Please choose a file to upload");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with the user id not found"));

        String key;
        try {
            key = s3Service.uploadFile(file, userId);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }

        File f = new File();
        f.setOwnerId(user);
        f.setFileName(file.getOriginalFilename());
        f.setFileKey(key);
        f.setType(FileType.FILE);
        f.setMimeType(file.getContentType());
        f.setFileSize(file.getSize());
        f.setStatus(FileStatus.DONE);

        try {
            fileRepository.save(f);
        } catch (Exception e) {
            s3Service.deleteFile(key);
            throw new InternalServerErrorException("Could not save this file. Please try again");
        }

        return new MessageResponseDTO(true, "File uploaded");
    }
}
