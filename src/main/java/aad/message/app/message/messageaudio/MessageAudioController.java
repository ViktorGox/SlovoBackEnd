package aad.message.app.message.messageaudio;

import aad.message.app.filetransfer.FileType;
import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.message.Message;
import aad.message.app.message.MessageRepository;
import aad.message.app.returns.Responses;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

//TODO: Probably later change to group/id/messagesaudio or /message/audio??
@RestController
@RequestMapping("/messagesaudio")
public class MessageAudioController {
    private final MessageRepository messageRepository;
    private final MessageAudioRepository messageAudioRepository;
    private final UserRepository userRepository;
    private final FileUploadHandler fileUploadHandler;

    public MessageAudioController(UserRepository userRepository,
                                  FileUploadHandler fileUploadHandler,
                                  MessageAudioRepository messageAudioRepository, MessageRepository messageRepository) {
        this.messageAudioRepository = messageAudioRepository;
        this.fileUploadHandler = fileUploadHandler;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @PostMapping
    public ResponseEntity<?> postMessage(@RequestPart(value = "file") MultipartFile file,
                                         @RequestPart(value = "dto") MessageAudioPostDTO dto) {
        Long userId = getUserId();
        // TODO: Check whether the user has access to the group.

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
        message.audioUrl = "placeholder";
        message.transcription = "dummy";

        message = messageAudioRepository.save(message);

        ResponseEntity<?> fileUploadResult = fileUploadHandler.uploadFile(file, FileType.MESSAGE_AUDIO, message.id);
        if (fileUploadResult.getStatusCode() != HttpStatus.OK) {
            messageRepository.delete(message);
            return fileUploadResult;
        }

        message.audioUrl = fileUploadHandler.okFileName(fileUploadResult);

        messageAudioRepository.save(message);

        return ResponseEntity.ok(new MessageAudioDTO(message));
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
