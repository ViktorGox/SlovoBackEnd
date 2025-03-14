package aad.message.app.message;

import aad.message.app.message.messageaudio.MessageAudio;
import aad.message.app.message.messageaudio.MessageAudioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {
    // TODO: Add text message equivalent stuff.
    private final MessageAudioRepository messageAudioRepository;

    @Autowired
    public MessageService(MessageAudioRepository messageAudioRepository) {
        this.messageAudioRepository = messageAudioRepository;
    }

    public List<Message> getMessagesByGroupId(Long groupId) {
        List<MessageAudio> audioMessages = messageAudioRepository.findByGroupId(groupId);

        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(audioMessages);

        return allMessages;
    }
}
