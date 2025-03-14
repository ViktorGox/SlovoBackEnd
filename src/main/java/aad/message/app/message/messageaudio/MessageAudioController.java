package aad.message.app.message.messageaudio;

import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//TODO: Probably later change to group/id/messagesaudio
@RestController
@RequestMapping("/messagesaudio")
public class MessageAudioController {
    private final MessageAudioRepository messageAudioRepository;
    private final UserRepository userRepository;
    private final FileUploadHandler fileUploadHandler;

    public MessageAudioController(UserRepository userRepository,
                                  FileUploadHandler fileUploadHandler,
                                  MessageAudioRepository messageAudioRepository) {
        this.messageAudioRepository = messageAudioRepository;
        this.fileUploadHandler = fileUploadHandler;
        this.userRepository = userRepository;
    }

    @GetMapping(path = "/{groupId}")
    public ResponseEntity<?> getMessagePerGroup(@PathVariable Long groupId) {
        // TODO: Check whether the user has access to the group.

        List<MessageAudioDTO> messages = messageAudioRepository.findByGroupId(groupId)
                .stream()
                .map(MessageAudioDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

//    public ResponseEntity<?> postMessage(@RequestPart(value = "file") MultipartFile file,
//                                         @RequestPart(value = "dto") MessageAudioPostDTO dto) {
//        Long userId = getUserId();
//        // TODO: Check whether the user has access to the group.
//        // TODO: Check if user exists? - Not sure if needed. See UserController.update for code.
//
//        MessageAudio message = new MessageAudio();
//        message.user = userRepository.getUserById(userId);
//        message.sentDate = LocalDateTime.now();
//        message.replyMessage = messageAudioRepository;
//
//        message = messageAudioRepository.save(message);
//
//        String fileName = "ma_" + LocalDateTime.now().toString() + "_" + message.id + ".mp3";
//        fileUploadHandler.uploadFile(file, fileName);
//
//        message.audioUrl = fileName;
//        messageAudioRepository.save(message);
//
//        return ResponseEntity.ok(new MessageAudioDTO(message));
//    }

    /**
     * Simple method to remove repetitive long line copies and pastes.
     *
     * @return the userId from the JWT token.
     */
    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
