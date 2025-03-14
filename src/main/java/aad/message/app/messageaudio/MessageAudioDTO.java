package aad.message.app.messageaudio;

import java.time.LocalDateTime;

public class MessageAudioDTO {
    public Long id;
    public String audioUrl;
    public String transcription;
    public Long userId;
    public Long replyMessageId;
    public LocalDateTime sentDate;
}
