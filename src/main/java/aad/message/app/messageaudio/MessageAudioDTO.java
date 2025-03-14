package aad.message.app.messageaudio;

import java.time.LocalDateTime;

public class MessageAudioDTO {
    public Long id;
    public String audioUrl;
    public String transcription;
    public Long userId;
    public Long replyMessageId;
    public LocalDateTime sentDate;

    public MessageAudioDTO() {}

    public MessageAudioDTO(MessageAudio message) {
        id = message.id;
        audioUrl = message.audioUrl;
        transcription = message.transcription;
        userId = message.user.id;
        replyMessageId = (message.replyMessage != null) ? message.replyMessage.id : null;
        sentDate = message.sentDate;
    }
}
