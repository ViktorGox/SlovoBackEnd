package aad.message.app.messageaudio;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//TODO: Probably later change to group/id/messagesaudio
@RestController
@RequestMapping("/messagesaudio")
public class MessageAudioController {
    private final MessageAudioRepository repository;

    public MessageAudioController(MessageAudioRepository repository) {
        this.repository = repository;
    }

    @GetMapping(path = "/{groupId}")
    public ResponseEntity<?> getMessagePerGroup(@PathVariable Long groupId) {
        // TODO: Check whether the user has access to the group.
        List<MessageAudio> messages = repository.findByGroups_Id(groupId);
        return ResponseEntity.ok(messages);
    }

    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
