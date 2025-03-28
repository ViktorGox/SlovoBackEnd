package aad.message.app.audio;

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
@RequestMapping("/audio")
public class AudioController {

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getAudioFile(@PathVariable String filename) throws IOException {
        Path path = Paths.get("src/main/resources/static/audio/" + filename);
        byte[] audioBytes = Files.readAllBytes(path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentDisposition(ContentDisposition.inline().filename(filename).build());

        return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
    }
}
