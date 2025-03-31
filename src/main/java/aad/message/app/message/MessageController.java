package aad.message.app.message;

import aad.message.app.message.messageaudio.MessageAudio;
import aad.message.app.message.messageaudio.MessageAudioDTO;
import aad.message.app.message.messagetext.MessageText;
import aad.message.app.message.messagetext.MessageTextDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getMessagesForGroup(@PathVariable Long groupId) {
        List<Message> messages = messageRepository.getMessagesByGroupId(groupId);

        List<MessageDTO> messageDTOs = messages.stream()
                .map(message -> {
                    if (message instanceof MessageText) {
                        return new MessageTextDTO((MessageText) message);
                    } else if (message instanceof MessageAudio) {
                        return new MessageAudioDTO((MessageAudio) message);
                    }
                    return null;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }
}