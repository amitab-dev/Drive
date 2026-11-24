package amitab.drive.contollers;

import amitab.drive.dto.file.DownloadFileRequestDto;
import amitab.drive.dto.file.FileItemDto;
import amitab.drive.dto.file.FileResponseDto;
import amitab.drive.dto.folder.CreateFolderRequestDTO;
import amitab.drive.entity.File;
import amitab.drive.entity.User;
import amitab.drive.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.DocFlavor;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    private final FileService fileService;

    @PostMapping("/{userId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable(name = "userId") UUID userId,
            @RequestBody DownloadFileRequestDto fileId,
            @AuthenticationPrincipal User user
    ) {
        File file = fileService.getFile(userId, fileId.key());
        InputStream inputStream = fileService.openStream(file);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .contentLength(file.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.getFileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(new InputStreamResource(inputStream));
    }

    @PostMapping("/{userId}/upload")
    public ResponseEntity<FileResponseDto> uploadFile(
            @PathVariable(name = "userId") UUID userId,
            @RequestParam(name = "file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.uploadFile(userId, file));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<FileItemDto>> listFiles(
            @PathVariable(name = "userId") UUID userId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.listFile(userId));
    }

    @DeleteMapping("/{userId}/{filename}")
    public ResponseEntity<FileItemDto> deleteFile(
            @PathVariable(name = "userId") UUID userId,
            @PathVariable(name = "filename") String fileName,
            @AuthenticationPrincipal User user
    ) {
        String key = userId + "/" + fileName;
        return ResponseEntity.status(HttpStatus.OK).body(fileService.deleteFile(key));
    }

    @PostMapping("/{userId}/upload-folder")
    public ResponseEntity<FileResponseDto> uploadFolder(
            @PathVariable(name = "userId") UUID userId,
            @RequestParam(name = "folder") MultipartFile folder
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.uploadFolder(userId, folder));
    }

    @PostMapping("/{userId}/create-folder")
    public ResponseEntity<FileResponseDto> createFolder(
            @PathVariable(name = "userId") UUID userId,
            @RequestBody CreateFolderRequestDTO body
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.createFolder(userId, body.folderName()));
    }

}
