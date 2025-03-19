package aad.message.app.message.messageadiogroup;

import java.io.Serializable;
import java.util.Objects;

public class MessageAudioGroupId implements Serializable {
    public Long messageAudio;
    public Long group;

    public MessageAudioGroupId() {}

    public MessageAudioGroupId(Long messageAudio, Long group) {
        this.messageAudio = messageAudio;
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageAudioGroupId that = (MessageAudioGroupId) o;
        return Objects.equals(messageAudio, that.messageAudio) &&
                Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageAudio, group);
    }
}
