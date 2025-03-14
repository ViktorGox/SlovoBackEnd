package aad.message.app.message.messagetext;

import aad.message.app.group.Group;
import aad.message.app.message.Message;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("text")
public class MessageText extends Message {
    @Column(name = "text", nullable = false)
    public String text;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    public Group group;
}
