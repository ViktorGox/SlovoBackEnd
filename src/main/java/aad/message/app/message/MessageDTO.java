package aad.message.app.message;

import java.time.LocalDateTime;

public class MessageDTO {
    public Long id;
    public Long userId;
    public LocalDateTime sentDate;
    public MessageType messageType;
    public Long replyToMessageId;
}
