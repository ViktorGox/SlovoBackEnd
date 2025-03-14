package aad.message.app.message.messageaudio;

import aad.message.app.message.MessageDTO;

public class MessageAudioDTO extends MessageDTO {
    public String audioUrl;
    public String transcription;

    public MessageAudioDTO() {}

    public MessageAudioDTO(MessageAudio message) {
        id = message.id;
        audioUrl = message.audioUrl;
        transcription = message.transcription;
        userId = message.user.id;
        messageType = message.messageType;
        replyToMessageId = (message.replyMessage != null) ? message.replyMessage.id : null;
        sentDate = message.sentDate;
    }
}
