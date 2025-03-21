package aad.message.app.message;

import aad.message.app.message.messageaudio.MessageAudio;
import aad.message.app.message.messageaudio.MessageAudioRepository;
import aad.message.app.message.messagetext.MessageText;
import aad.message.app.message.messagetext.MessageTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {
    private final MessageAudioRepository messageAudioRepository;
    private final MessageTextRepository messageTextRepository;

    @Autowired
    public MessageService(MessageAudioRepository messageAudioRepository, MessageTextRepository messageTextRepository) {
        this.messageAudioRepository = messageAudioRepository;
        this.messageTextRepository = messageTextRepository;
    }

    public List<Message> getMessagesByGroupId(Long groupId) {
        List<MessageAudio> audioMessages = messageAudioRepository.findByGroupId(groupId);
        List<MessageText> textMessages = messageTextRepository.findByGroupId(groupId);

        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(audioMessages);
        allMessages.addAll(textMessages);

        return allMessages;
    }
}
