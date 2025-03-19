package aad.message.app.message.messageaudio;

import aad.message.app.filetransfer.FileType;
import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.group.GroupRepository;
import aad.message.app.message.Message;
import aad.message.app.message.MessageRepository;
import aad.message.app.message.MessageType;
import aad.message.app.message.messageadiogroup.MessageAudioGroup;
import aad.message.app.message.messageadiogroup.MessageAudioGroupRepository;
import aad.message.app.message.messageaudio.transcription.TranscriptionService;
import aad.message.app.returns.Responses;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/message/audio")
public class MessageAudioController {
    private final MessageRepository messageRepository;
    private final MessageAudioRepository messageAudioRepository;
    private final UserRepository userRepository;
    private final FileUploadHandler fileUploadHandler;
    private final MessageAudioGroupRepository messageAudioGroupRepository;
    private final GroupRepository groupRepository;
    private final TranscriptionService transcriptionService;

    public MessageAudioController(UserRepository userRepository,
                                  FileUploadHandler fileUploadHandler,
                                  MessageAudioRepository messageAudioRepository,
                                  MessageRepository messageRepository,
                                  MessageAudioGroupRepository messageAudioGroupRepository,
                                  GroupRepository groupRepository,
                                  TranscriptionService transcriptionService) {
        this.messageAudioRepository = messageAudioRepository;
        this.fileUploadHandler = fileUploadHandler;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageAudioGroupRepository = messageAudioGroupRepository;
        this.groupRepository = groupRepository;
        this.transcriptionService = transcriptionService;
    }

    @PostMapping
    public ResponseEntity<?> postMessage(@RequestPart(value = "file") MultipartFile file,
                                         @RequestPart(value = "dto") MessageAudioPostDTO dto) throws IOException {
        Long userId = getUserId();

        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) return Responses.impossibleUserNotFound(userId);

        MessageAudio message = new MessageAudio();
        message.user = user.get();
        message.sentDate = LocalDateTime.now();

        if(dto.replyMessageId != null) {
            Optional<Message> reply = messageRepository.findMessageById(dto.replyMessageId);
            if(reply.isPresent()) {
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

    /**
     * Simple method to remove repetitive long line copies and pastes.
     *
     * @return the userId from the JWT token.
     */
    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
