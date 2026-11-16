package amitab.drive.contollers;

import amitab.drive.dto.file.FileResponseDto;
import amitab.drive.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    private final FileService fileService;

    @PostMapping("/{userId}/upload")
    public ResponseEntity<FileResponseDto> uploadFile(
            @PathVariable(name = "userId") UUID userId,
            @RequestParam(name = "file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.uploadFile(userId, file));
    }

    @PostMapping("/{userId}/upload-folder")
    public ResponseEntity<FileResponseDto> uploadFolder(
            @PathVariable(name = "userId") UUID userId,
            @RequestParam(name = "folder") MultipartFile folder
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.uploadFolder(userId, folder));
    }


    public ResponseEntity<FileResponseDto> createFolder(
            @RequestParam(name = "userId") UUID userId,
            @RequestBody String folderName
    ) {
        return  ResponseEntity.status(HttpStatus.OK).body(fileService.createFolder(userId, folderName));
    }

}
