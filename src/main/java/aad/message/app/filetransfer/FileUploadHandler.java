package aad.message.app.filetransfer;

import aad.message.app.returns.Responses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Component
public class FileUploadHandler {
    private static final String UPLOAD_DIR = "uploads";

    public ResponseEntity<?> uploadFile(MultipartFile file, FileType fileType, Long id) {
        if (file.isEmpty()) return Responses.error("No file provided");

        String fileExtension = getFileExtension(file);

        ResponseEntity<?> fileValidityResponse = isFileValid(file, fileType, fileExtension);
        if (fileValidityResponse.getStatusCode() != HttpStatus.OK) return fileValidityResponse;

        String fileName = generateName(fileType, id, fileExtension);

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

    private ResponseEntity<?> isFileValid(MultipartFile file, FileType fileType, String fileExtension) {
        switch (fileType) {
            case FileType.MESSAGE_AUDIO -> {
                List<String> allowedExtensions = List.of("mp3", "wav");
                if(!isValidAudioMimeType(file)) return Responses.error("Invalid file type");
                if(!allowedExtensions.contains(fileExtension)) return Responses.error("Invalid file extension");
            }
            case FileType.PROFILE_PICTURE -> {
                List<String> allowedExtensions = List.of("png", "jpeg", "jpg");
                if(!isValidImageMimeType(file)) return Responses.error("Invalid file type");
                if(!allowedExtensions.contains(fileExtension)) return Responses.error("Invalid file extension");
            }
        }
        return ResponseEntity.ok().build();
    }

    private String generateName(FileType fileType, Long id, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String formatted = LocalDateTime.now().format(formatter);
        return fileType.getShortName() + formatted + id + "." + extension;
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        return "";
    }

    public boolean isValidAudioMimeType(MultipartFile file) {
        List<String> allowedMimeTypes = List.of("audio/wav", "audio/mpeg");
        return isValidMimeType(file, allowedMimeTypes);
    }

    public boolean isValidImageMimeType(MultipartFile file) {
        List<String> allowedMimeTypes = List.of("image/png", "image/jpeg");
        return isValidMimeType(file, allowedMimeTypes);
    }

    private boolean isValidMimeType(MultipartFile file, Collection<String> mimeTypes) {
        String mimeType = file.getContentType();
        return mimeTypes.contains(mimeType);
    }
}
