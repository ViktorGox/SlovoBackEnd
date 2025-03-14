package aad.message.app.message.messageaudio;

import aad.message.app.group.Group;
import aad.message.app.message.Message;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("audio")
public class MessageAudio extends Message {

    @Column(name = "audio_url", nullable = false)
    public String audioUrl;

    @Column(name = "transcription", nullable = false)
    public String transcription;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "message_audio_group",
            joinColumns = @JoinColumn(name = "message_audio_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    public Set<Group> groups = new HashSet<>();
}
