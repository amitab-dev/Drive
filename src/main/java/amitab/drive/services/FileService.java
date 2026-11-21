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
import java.io.OutputStream;
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

        UUID fileId = UUID.randomUUID();
        String key = userId + "/" + fileId + "::" + file.getOriginalFilename();

        try {
            s3Service.uploadFile(file, key);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }

        File f = new File();
        f.setId(fileId);
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
        List<File> files = fileRepository.findByOwner(owner);
        List<FileItemDto> response = new ArrayList<>();
        for (File f : files) {
            response.add(FileItemDto.from(f));
        }
        return response;
    }

    public byte[] downloadFile(UUID userId, String fileId) {
        String key = userId + "/" + fileId;
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        fileRepository.findByOwner(owner)
                .stream()
                .filter(file -> file.getFileKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("File not found in DB"));

        try {
            return s3Service.downloadFile(key);
        } catch (IOException ex) {
            throw new RuntimeException("File not found");
        }
    }

    public void downloadLargeFile(File file, OutputStream outputStream) {
        String key = file.getOwner() + "/" + file.getFileKey();
        try {
            s3Service.downloadLargeFile(key, outputStream);
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

    public File getFile(UUID userId, String fileId) {
        String key = userId + "/" + fileId;
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return fileRepository.findByOwner(owner)
                .stream()
                .filter(file -> file.getFileKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("File not found in DB"));
    }
}
