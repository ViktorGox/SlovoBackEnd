package aad.message.app.message.messagetext;

import aad.message.app.group.GroupRepository;
import aad.message.app.group_user_role.GroupUserRoleRepository;
import aad.message.app.message.Message;
import aad.message.app.message.MessageRepository;
import aad.message.app.message.MessageType;
import aad.message.app.middleware.GroupAccessInterceptor;
import aad.message.app.returns.Responses;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import aad.message.app.websocket.SlovoWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages/text")
public class MessageTextController {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageTextRepository messageTextRepository;
    private final GroupRepository groupRepository;
    private final GroupUserRoleRepository groupUserRoleRepository;
    private final SlovoWebSocketHandler webSocketHandler;

    public MessageTextController(UserRepository userRepository,
                                 MessageRepository messageRepository,
                                 MessageTextRepository messageTextRepository,
                                 GroupRepository groupRepository,
                                 GroupUserRoleRepository groupUserRoleRepository,
                                 SlovoWebSocketHandler webSocketHandler) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageTextRepository = messageTextRepository;
        this.groupRepository = groupRepository;
        this.groupUserRoleRepository = groupUserRoleRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping
    public ResponseEntity<?> postMessage(@RequestBody(required = false) MessageTextPostDTO dto) {
        if (dto == null) return Responses.incompleteBody(List.of("MessageTextPostDTO"));
        Collection<String> missingFields = MessageTextPostDTO.verify(dto);
        if (!missingFields.isEmpty()) return Responses.incompleteBody(missingFields);

        Long userId = getUserId();
        if (GroupAccessInterceptor.isUnauthorizedForGroup(groupUserRoleRepository, List.of(dto.groupId))) {
            return Responses.unauthorized();
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return Responses.impossibleUserNotFound(userId);

        MessageText message = new MessageText();
        message.user = user.get();
        message.sentDate = LocalDateTime.now(ZoneOffset.UTC);

        if (dto.replyMessageId != null) {
            Optional<Message> reply = messageRepository.findById(dto.replyMessageId);
            reply.ifPresent(value -> message.replyMessage = value);
        }
        message.messageType = MessageType.text;
        message.text = dto.text;
        message.group = groupRepository.findGroupById(dto.groupId);

        messageTextRepository.save(message);

        webSocketHandler.sendMessageToGroup(dto.groupId, message.id);

        return ResponseEntity.ok(new MessageTextDTO(message));
    }

    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
