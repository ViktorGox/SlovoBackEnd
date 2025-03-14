package aad.message.app.message.messagetext;

import aad.message.app.message.MessageDTO;

public class MessageTextDTO extends MessageDTO {
    public String text;
    public Long groupId;

    public MessageTextDTO() {}

    public MessageTextDTO(MessageText message) {
        id = message.id;
        text = message.text;
        groupId = message.group.id;
        userId = message.user.id;
        messageType = message.messageType;
        replyToMessageId = (message.replyMessage != null) ? message.replyMessage.id : null;
        sentDate = message.sentDate;
    }
}
