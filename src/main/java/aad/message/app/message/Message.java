package aad.message.app.message;

import aad.message.app.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "message_type")
@Table(name = "message")
public abstract class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(name = "sent_date", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    public OffsetDateTime sentDate;

    @Column(name = "message_type", nullable = false, insertable=false, updatable=false)
    @Enumerated(EnumType.STRING)
    public MessageType messageType;

    @ManyToOne
    @JoinColumn(name = "reply_to_message_id")
    public Message replyMessage;
}

