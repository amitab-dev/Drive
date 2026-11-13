package amitab.drive.services;

import amitab.drive.dto.MessageResponseDTO;
import amitab.drive.entity.File;
import amitab.drive.entity.FileStatus;
import amitab.drive.entity.FileType;
import amitab.drive.entity.User;
import amitab.drive.exception.BadRequestException;
import amitab.drive.repository.FileRepository;
import amitab.drive.repository.UserRepository;
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

    public MessageResponseDTO uploadFile(UUID userId, MultipartFile file) {
        User owner = userRepository.getReferenceById(userId);
        String fileName = file.getOriginalFilename();
        String baseName = owner.getId() + "/" + fileName;

        try {
            s3Service.uploadFile(baseName, file.getInputStream(), file.getSize());
        } catch (IOException ex) {
            throw new BadRequestException("File not found");
        }

        File f = new File();
        f.setOwnerId(owner);
        f.setName(fileName);
        f.setType(FileType.FILE);
        f.setMimeType(file.getContentType());
        f.setFileSize(file.getSize());
        f.setStatus(FileStatus.PENDING);
        fileRepository.save(f);

        return new MessageResponseDTO(true, "File uploaded");
    }
}
