package amitab.drive.services;

import amitab.drive.dto.file.FileItemDto;
import amitab.drive.dto.file.FileResponseDto;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Service s3Service;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    @Transactional
    public FileResponseDto uploadFile(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file found, Please choose a file to upload");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with the user id not found"));

        String key;
        try {
            key = s3Service.uploadFile(file, user.getId());
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }

        File f = new File();
        f.setOwner(user);
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
        return FileResponseDto.from(f);
    }

    @Transactional
    public FileResponseDto uploadFolder(UUID userId, MultipartFile folder) {
        return null;
    }

    public FileResponseDto createFolder(UUID userId, String folderName) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        String key = userId + "/" + folderName;
        if (fileRepository.findByFileKey(key).isPresent()) {
            throw new BadRequestException("Folder exists");
        }
        String folderKey = s3Service.createFolder(folderName, userId);
        return FileResponseDto.from(folderKey);
    }

    public List<FileItemDto> listFile(UUID userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<File> files = fileRepository.findByOwnerAndTrashedAtIsNull(owner);
        List<FileItemDto> response = new ArrayList<>();
        for (File f : files) {
            response.add(FileItemDto.from(f));
        }
        return response;
    }

    public File getFile(UUID userId, String fileId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return fileRepository.findByFileKey(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));
    }

    public InputStream openStream(File file) {
        String key = file.getFileKey();
        try {
            return s3Service.openStream(key);
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

    @Transactional
    public FileItemDto deleteFile(String key) {
        File file = fileRepository.findByFileKey(key)
                .orElseThrow(() -> new NotFoundException("File not found"));
        file.setTrashedAt(Instant.now());
        return FileItemDto.from(file);
    }
}
