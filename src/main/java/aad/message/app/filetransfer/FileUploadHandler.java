package aad.message.app.filetransfer;

import aad.message.app.returns.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class FileUploadHandler {
    private static final String UPLOAD_DIR = "uploads";

    public Optional<ResponseEntity<?>> uploadFile(MultipartFile file, String fileName) {
        if (file.isEmpty()) {
            return Optional.of(Responses.Error("No file provided"));
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(Responses.InternalError("Failed to upload file: " + e.getMessage()));
        }
    }

    public String generateName(FileType filePrefix, Long id) {
        return filePrefix.getShortName() + LocalDateTime.now() + id;
    }
}
