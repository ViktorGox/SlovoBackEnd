package aad.message.app.message;

import java.time.LocalDateTime;

public class RecentMessageDTO {
    public String username;
    public MessageType messageType;
    public String messageText;
    public LocalDateTime lastMessageTime;
}
