package aad.message.app.files;

import aad.message.app.returns.Responses;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    @GetMapping("/images/{filename}")
    public ResponseEntity<byte[]> getImageFile(@PathVariable String filename) {
        String contentType = "";
        if (filename.endsWith(".png")) {
            contentType = "image/png";
        } else if (filename.endsWith(".jpeg") || filename.endsWith(".jpg")) {
            contentType =  "image/jpeg";
        }

        return getFile("src/main/resources/static/images/", filename, contentType);
    }

    @GetMapping("/audio/{filename}")
    public ResponseEntity<byte[]> getAudioFile(@PathVariable String filename) {
        String contentType = "";
        if (filename.endsWith(".mp3")) {
            contentType = "audio/mpeg";
        } else if (filename.endsWith(".wav")) {
            contentType = "audio/wav";
        } else if (filename.endsWith(".aac")) {
            contentType = "audio/aac";
        }

        return getFile("src/main/resources/static/audio/", filename, contentType);
    }

    private ResponseEntity<byte[]> getFile(String stringPath, String fileName, String mediaType) {
        Path path = Paths.get(stringPath + "/" + fileName);

        if (!Files.exists(path)) Responses.notFound(stringPath);

        byte[] audioBytes = null;
        try {
            audioBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            Responses.internalError("Something went wrong while reading " + stringPath + ".");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        headers.setContentDisposition(ContentDisposition.inline().filename(fileName).build());

        return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
    }
}
