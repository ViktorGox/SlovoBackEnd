package aad.message.app.group;

import aad.message.app.message.RecentMessageDTO;

import java.time.LocalDateTime;

public class RecentChatDTO {
    public Long groupId;
    public String groupTitle;
    public String groupImage;
    public LocalDateTime lastUserMessageTime;
    public RecentMessageDTO lastMessage;
}
