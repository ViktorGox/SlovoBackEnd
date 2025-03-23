package aad.message.app.message.messageaudio;

import aad.message.app.filetransfer.FileType;
import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.group.GroupRepository;
import aad.message.app.group_user.GroupUserRoleRepository;
import aad.message.app.message.Message;
import aad.message.app.message.MessageRepository;
import aad.message.app.message.MessageType;
import aad.message.app.message.messageadiogroup.MessageAudioGroup;
import aad.message.app.message.messageadiogroup.MessageAudioGroupRepository;
import aad.message.app.message.messageaudio.transcription.TranscriptionService;
import aad.message.app.middleware.GroupAccessInterceptor;
import aad.message.app.returns.Responses;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages/audio")
public class MessageAudioController {
    private final MessageRepository messageRepository;
    private final MessageAudioRepository messageAudioRepository;
    private final UserRepository userRepository;
    private final FileUploadHandler fileUploadHandler;
    private final MessageAudioGroupRepository messageAudioGroupRepository;
    private final GroupRepository groupRepository;
    private final TranscriptionService transcriptionService;
    private final GroupUserRoleRepository groupUserRoleRepository;

    public MessageAudioController(UserRepository userRepository,
                                  FileUploadHandler fileUploadHandler,
                                  MessageAudioRepository messageAudioRepository,
                                  MessageRepository messageRepository,
                                  MessageAudioGroupRepository messageAudioGroupRepository,
                                  GroupRepository groupRepository,
                                  TranscriptionService transcriptionService, GroupUserRoleRepository groupUserRoleRepository) {
        this.messageAudioRepository = messageAudioRepository;
        this.fileUploadHandler = fileUploadHandler;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageAudioGroupRepository = messageAudioGroupRepository;
        this.groupRepository = groupRepository;
        this.transcriptionService = transcriptionService;
        this.groupUserRoleRepository = groupUserRoleRepository;
    }

    @PostMapping
    public ResponseEntity<?> postMessage(@RequestPart(value = "file", required = false) MultipartFile file,
                                         @RequestPart(value = "dto", required = false) MessageAudioPostDTO dto)
            throws IOException {
        if(dto == null) return Responses.incompleteBody(List.of("MessageAudioPostDTO"));
        if(file == null || file.isEmpty()) return Responses.incompleteBody(List.of("File"));

        Collection<String> missingFields = MessageAudioPostDTO.verify(dto);
        if (!missingFields.isEmpty()) return Responses.incompleteBody(missingFields);

        Long userId = getUserId();
        if (GroupAccessInterceptor.isUnauthorizedForGroup(groupUserRoleRepository, dto.groupIds)) {
            return Responses.unauthorized();
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return Responses.impossibleUserNotFound(userId);

        MessageAudio message = new MessageAudio();
        message.user = user.get();
        message.sentDate = LocalDateTime.now();

        if (dto.replyMessageId != null) {
            Optional<Message> reply = messageRepository.findMessageById(dto.replyMessageId);
            if (reply.isPresent()) {
                message.replyMessage = reply.get();
            }
        }
        message.messageType = MessageType.audio;
        message.audioUrl = "";
        message.transcription = "";

        message = messageAudioRepository.save(message);

        ResponseEntity<?> fileUploadResult = fileUploadHandler.uploadFile(file, FileType.MESSAGE_AUDIO, message.id);
        if (fileUploadResult.getStatusCode() != HttpStatus.OK) {
            messageRepository.delete(message);
            return fileUploadResult;
        }

        message.audioUrl = fileUploadHandler.okFileName(fileUploadResult);

        messageAudioRepository.save(message);

        saveMessageAudioGroups(message, dto.groupIds);

        transcriptionService.transcribeAudio(message);

        return ResponseEntity.ok(new MessageAudioDTO(message));
    }

    private void saveMessageAudioGroups(MessageAudio messageAudio, List<Long> groupIds) {
        List<MessageAudioGroup> messageAudioGroups = groupIds.stream()
                .map(groupId -> new MessageAudioGroup(messageAudio, groupRepository.findGroupById(groupId)))
                .collect(Collectors.toList());
        messageAudioGroupRepository.saveAll(messageAudioGroups);
    }

    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
