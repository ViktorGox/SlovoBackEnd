package aad.message.app.message;

import aad.message.app.message.messageaudio.MessageAudio;
import aad.message.app.message.messageaudio.MessageAudioDTO;
import aad.message.app.message.messagetext.MessageText;
import aad.message.app.message.messagetext.MessageTextDTO;
import aad.message.app.returns.Responses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/group/{groupId}/{page}")
    public ResponseEntity<?> getMessagesForGroup(@PathVariable Long groupId, @PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 15, Sort.by("sentDate").descending());
        Page<Message> messagesPage = messageRepository.getMessagesByGroupId(groupId, pageable);
        List<Message> messages = messagesPage.getContent();

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

    @GetMapping("/{groupId}/{id}")
    public ResponseEntity<?> getMessageById(@PathVariable Long id, @PathVariable String groupId) {
        Optional<Message> messageOptional = messageRepository.findById(id);
        if (messageOptional.isEmpty()) {
            return Responses.notFound(id.toString());
        }

        Message message = messageOptional.get();

        if (message instanceof MessageText) {
            return ResponseEntity.ok(new MessageTextDTO((MessageText) message));
        } else if (message instanceof MessageAudio) {
            return ResponseEntity.ok(new MessageAudioDTO((MessageAudio) message));
        }
        return Responses.internalError("Message could not be casted to a known message class.");
    }
}