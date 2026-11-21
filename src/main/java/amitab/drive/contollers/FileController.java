package amitab.drive.contollers;

import amitab.drive.dto.file.FileItemDto;
import amitab.drive.dto.file.FileResponseDto;
import amitab.drive.dto.folder.CreateFolderRequestDTO;
import amitab.drive.entity.User;
import amitab.drive.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
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

    @GetMapping("/{userId}")
    public ResponseEntity<List<FileItemDto>> listFiles(
            @PathVariable(name = "userId") UUID userId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.listFile(userId));
    }

    @GetMapping("/{userId}/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable(name = "userId") UUID userId,
            @PathVariable(name = "fileId") String fileId,
            @AuthenticationPrincipal User user
    ) {
        DownloadFileDto file = fileService.downloadFile(userId, user, fileId);

        MediaType contentType;
        try {
            contentType = file.contentType() != null
                    ? MediaType.parseMediaType(file.contentType())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (InvalidMediaTypeException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .contentType(contentType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                );

        if (file.contentLength() != null) {
            response.contentLength(file.contentLength());
        }

        return response.body(new InputStreamResource(file.stream()));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<String> deleteFile(
            @PathVariable(name = "keyId") String keyId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body("Deleted");
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
