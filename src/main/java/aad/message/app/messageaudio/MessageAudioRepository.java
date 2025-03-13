package aad.message.app.messageaudio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageAudioRepository extends JpaRepository<MessageAudio, Long> {
    List<MessageAudio> findByGroups_Id(Long groupId);
}
