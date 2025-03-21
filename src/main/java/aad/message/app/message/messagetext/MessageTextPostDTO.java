package aad.message.app.message.messagetext;


import java.util.ArrayList;
import java.util.Collection;

public class MessageTextPostDTO {
    public Long replyMessageId;
    public Long groupId;
    public String text;

    public static Collection<String> verify(MessageTextPostDTO dto) {
        ArrayList<String> list = new ArrayList<>();

        // reply message id can be null.
        if(dto.groupId == null) list.add("groupIds is null");
        if(dto.text == null || dto.text.isBlank()) list.add("text is null or empty");

        return list;
    }
}
