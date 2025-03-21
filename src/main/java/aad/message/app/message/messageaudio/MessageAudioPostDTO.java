package aad.message.app.message.messageaudio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageAudioPostDTO {
    public Long replyMessageId;
    public List<Long> groupIds;

    public static Collection<String> verify(MessageAudioPostDTO dto) {
        ArrayList<String> list = new ArrayList<>();

        // reply message id can be null.
        if(dto.groupIds == null) list.add("groupIds is null");
        else if(dto.groupIds.isEmpty()) list.add("Group array is empty.");

        return list;
    }
}
