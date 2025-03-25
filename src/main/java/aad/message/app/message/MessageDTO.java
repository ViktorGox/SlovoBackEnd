package aad.message.app.message;

import java.time.OffsetDateTime;

public class MessageDTO {
    public Long id;
    public Long userId;
    public OffsetDateTime sentDate;
    public MessageType messageType;
    public Long replyToMessageId;
}
