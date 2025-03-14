package aad.message.app.message;

import aad.message.app.message.messageaudio.MessageAudio;
import aad.message.app.message.messageaudio.MessageAudioDTO;
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

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getMessagesForGroup(@PathVariable Long groupId) {
        List<Message> messages = messageService.getMessagesByGroupId(groupId);

        List<MessageDTO> messageDTOs = messages.stream()
                .map(message -> {
                    // TODO: Add text message
                    if (message instanceof MessageAudio) {
                        return new MessageAudioDTO((MessageAudio) message);
                    }
                    return null;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }
}