package aad.message.app.message.messageaudio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageAudioRepository extends JpaRepository<MessageAudio, Long> {
    @Query("SELECT ma FROM MessageAudio ma JOIN ma.groups g WHERE g.id = :groupId")
    List<MessageAudio> findByGroupId(@Param("groupId") Long groupId);
}
