package aad.message.app.filetransfer;

import aad.message.app.returns.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FileUploadHandler {
    private static final String UPLOAD_DIR = "uploads";

    public ResponseEntity<?> uploadFile(MultipartFile file, FileType fileType, Long id) {
        if (file.isEmpty()) return Responses.error("No file provided");

        String fileName = generateName(file, fileType, id);

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            return Responses.internalError("Failed to upload file: " + e.getMessage());
        }
    }

    private String generateName(MultipartFile file, FileType filePrefix, Long id) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String formatted = LocalDateTime.now().format(formatter);
        return filePrefix.getShortName() + formatted + id + getFileExtension(file);
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "";
    }
}
