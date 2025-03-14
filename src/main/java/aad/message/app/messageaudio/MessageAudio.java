package aad.message.app.messageaudio;

import aad.message.app.group.Group;
import aad.message.app.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "message_audio")
public class MessageAudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "audio_url", nullable = false)
    public String audioUrl;

    @Column(name = "transcription", nullable = false)
    public String transcription;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne
    @JoinColumn(name = "reply_message_id")
    public MessageAudio replyMessage;

    @Column(name = "sent_date", nullable = false)
    public LocalDateTime sentDate;

    @JsonIgnore // TODO: Currently if not ignored, throws an null pointer exception.
                //  Not used anyway, so didn't bother to fix it.
    @ManyToMany
    @JoinTable(
            name = "message_audio_group",
            joinColumns = @JoinColumn(name = "message_audio_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    public Set<Group> groups = new HashSet<>();
}

