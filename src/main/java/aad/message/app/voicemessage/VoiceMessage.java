package aad.message.app.voicemessage;

import aad.message.app.group.Group;
import aad.message.app.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "message_audio")
public class VoiceMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "audio_url", nullable = false)
    public String audioUrl;

    @Column(name = "transcription")
    public String transcription;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne
    @JoinColumn(name = "reply_message_id")
    public VoiceMessage replyMessage;

    @Column(name = "sent_date", nullable = false)
    public LocalDateTime sentDate;

    @ManyToMany
    @JoinTable(
            name = "message_audio_group",
            joinColumns = @JoinColumn(name = "message_audio_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    public Set<Group> groups = new HashSet<>();
}

