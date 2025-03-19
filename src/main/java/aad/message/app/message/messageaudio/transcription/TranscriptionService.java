package aad.message.app.message.messageaudio.transcription;

import aad.message.app.message.messageaudio.MessageAudio;
import aad.message.app.message.messageaudio.MessageAudioRepository;
import aad.message.app.message.messageaudio.transcription.deepgram.DeepgramResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class TranscriptionService {
    private final MessageAudioRepository messageAudioRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${deepgram.api.key}")
    private String apiKey;

    @Value("${deepgram.file.path}")
    private String uploadDir;

    public TranscriptionService(MessageAudioRepository messageAudioRepository) {
        this.messageAudioRepository = messageAudioRepository;
    }

    @Async
    public void transcribeAudio(MessageAudio message) throws IOException {
        // TODO: This is kind of temporary, in AWS there won't be a path, it will be a link.
        File savedFile = new File(uploadDir + message.audioUrl);

        String configDeepgramModel = "nova-3";

        String apiUrl = "https://api.deepgram.com/v1/listen?model=" + configDeepgramModel + "&smart_format=true&detect_language=true";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + apiKey);
        headers.setContentType(MediaType.parseMediaType("deepgram/audio+video"));

        byte[] fileContent = Files.readAllBytes(savedFile.toPath());
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileContent, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        DeepgramResponse deepgramModel = objectMapper.readValue(response.getBody(), DeepgramResponse.class);

        message.transcription = deepgramModel.results.channels.getFirst().alternatives.getFirst().transcript;
        messageAudioRepository.save(message);
    }
}
