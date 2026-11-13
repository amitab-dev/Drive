package amitab.drive.contollers;

import amitab.drive.dto.MessageResponseDTO;
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
    public ResponseEntity<MessageResponseDTO> uploadFile(
            @PathVariable(name = "userId") UUID userId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.uploadFile(userId, file));
    }

}
