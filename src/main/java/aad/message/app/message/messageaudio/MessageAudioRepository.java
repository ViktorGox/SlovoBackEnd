package aad.message.app.message.messageaudio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageAudioRepository extends JpaRepository<MessageAudio, Long> {
    @Query("SELECT ma FROM MessageAudio ma JOIN ma.groups g WHERE g.id = :groupId")
    List<MessageAudio> findByGroupId(@Param("groupId") Long groupId);
    @Query("SELECT ma FROM MessageAudio ma " +
            "JOIN MessageAudioGroup mag ON ma.id = mag.messageAudio.id " +
            "JOIN mag.group g " +
            "WHERE g.id = :groupId AND mag.messageAudio.user.id = :userId " +
            "ORDER BY ma.sentDate " +
            "LIMIT 1")
    Optional<MessageAudio> findTopByUserIdAndGroupIdOrderBySentDateDesc(@Param("userId") Long userId,
                                                                        @Param("groupId") Long groupId);
    List<MessageAudio> findAllByGroups_Id(Long groupId);
}

