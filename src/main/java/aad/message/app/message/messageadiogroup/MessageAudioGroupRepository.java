package aad.message.app.message.messageadiogroup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageAudioGroupRepository extends JpaRepository<MessageAudioGroup, MessageAudioGroupId> {
    Optional<MessageAudioGroup> findTopByGroupIdOrderByMessageAudioSentDateDesc(Long group_id);

}