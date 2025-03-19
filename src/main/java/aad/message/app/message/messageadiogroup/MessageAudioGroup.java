package aad.message.app.message.messageadiogroup;

import aad.message.app.group.Group;
import aad.message.app.message.messageaudio.MessageAudio;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "message_audio_group")
@IdClass(MessageAudioGroupId.class)
public class MessageAudioGroup {
    @Id
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "message_audio_id", referencedColumnName = "id")
    public MessageAudio messageAudio;

    @Id
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    public Group group;

    public MessageAudioGroup() {}

    public MessageAudioGroup(MessageAudio messageAudio, Group group) {
        this.messageAudio = messageAudio;
        this.group = group;
    }
}
