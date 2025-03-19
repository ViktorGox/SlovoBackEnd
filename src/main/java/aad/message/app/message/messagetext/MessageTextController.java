package aad.message.app.message.messagetext;

import aad.message.app.group.GroupRepository;
import aad.message.app.message.Message;
import aad.message.app.message.MessageRepository;
import aad.message.app.message.MessageType;
import aad.message.app.returns.Responses;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/messages/text")
public class MessageTextController {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageTextRepository messageTextRepository;
    private final GroupRepository groupRepository;

    public MessageTextController(UserRepository userRepository,
                                 MessageRepository messageRepository,
                                 MessageTextRepository messageTextRepository,
                                 GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageTextRepository = messageTextRepository;
        this.groupRepository = groupRepository;
    }

    @PostMapping
    public ResponseEntity<?> postMessage(@RequestBody MessageTextPostDTO dto) {
        Long userId = getUserId();

        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) return Responses.impossibleUserNotFound(userId);

        MessageText message = new MessageText();
        message.user = user.get();
        message.sentDate = LocalDateTime.now();

        if(dto.replyMessageId != null) {
            Optional<Message> reply = messageRepository.findMessageById(dto.replyMessageId);
            if(reply.isPresent()) {
                message.replyMessage = reply.get();
            }
        }
        message.messageType = MessageType.text;
        message.text = dto.text;
        message.group = groupRepository.findGroupById(dto.groupId);

        messageTextRepository.save(message);

        return ResponseEntity.ok(new MessageTextDTO(message));
    }

    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
