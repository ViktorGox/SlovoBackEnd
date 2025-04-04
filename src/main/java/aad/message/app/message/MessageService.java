package aad.message.app.message;

import aad.message.app.message.messageadiogroup.MessageAudioGroup;
import aad.message.app.message.messageadiogroup.MessageAudioGroupRepository;
import aad.message.app.message.messageaudio.MessageAudio;
import aad.message.app.message.messageaudio.MessageAudioRepository;
import aad.message.app.message.messagetext.MessageText;
import aad.message.app.message.messagetext.MessageTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageService {
    private final MessageAudioRepository messageAudioRepository;
    private final MessageTextRepository messageTextRepository;
    private final MessageAudioGroupRepository messageAudioGroupRepository;

    @Autowired
    public MessageService(MessageAudioRepository messageAudioRepository, MessageTextRepository messageTextRepository, MessageAudioGroupRepository messageAudioGroupRepository) {
        this.messageAudioRepository = messageAudioRepository;
        this.messageTextRepository = messageTextRepository;
        this.messageAudioGroupRepository = messageAudioGroupRepository;
    }

    public Optional<Message> getLatestMessageByUser(Long userId, Long groupId) {
        Optional<MessageText> userTextMessage = messageTextRepository.findTopByUserIdAndGroupIdOrderBySentDateDesc(userId, groupId);

        Optional<MessageAudio> userAudioMessage = messageAudioRepository.findTopByUserIdAndGroupIdOrderBySentDateDesc(userId, groupId);

        if (userTextMessage.isPresent() && userAudioMessage.isPresent()) {
            return userTextMessage.get().sentDate.isAfter(userAudioMessage.get().sentDate) ?
                    Optional.of(userTextMessage.get()) : Optional.of(userAudioMessage.get());
        } else if (userTextMessage.isPresent()) {
            return userTextMessage.map(messageText -> messageText);
        } else {
            return userAudioMessage.map(messageAudio -> messageAudio);
        }
    }

    public Optional<Message> getLatestMessageForGroup(Long groupId) {
        Optional<MessageText> groupTextMessage = messageTextRepository.findTopByGroupIdOrderBySentDateDesc(groupId);

        Optional<MessageAudioGroup> groupAudioMessage = messageAudioGroupRepository.findTopByGroupIdOrderByMessageAudioSentDateDesc(groupId);

        if (groupTextMessage.isPresent() && groupAudioMessage.isPresent()) {
            return groupTextMessage.get().sentDate.isAfter(groupAudioMessage.get().messageAudio.sentDate) ?
                    Optional.of(groupTextMessage.get()) : Optional.of(groupAudioMessage.get().messageAudio);
        } else if (groupTextMessage.isPresent()) {
            return groupTextMessage.map(messageText -> messageText);
        } else {
            return groupAudioMessage.map(messageAudioGroup -> messageAudioGroup.messageAudio);
        }
    }
}
