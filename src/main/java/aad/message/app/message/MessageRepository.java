package aad.message.app.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
                SELECT m FROM Message m 
                LEFT JOIN MessageText mt ON m.id = mt.id 
                LEFT JOIN MessageAudio ma ON m.id = ma.id 
                LEFT JOIN ma.groups g 
                WHERE mt.group.id = :groupId OR g.id = :groupId
            """)
    Page<Message> getMessagesByGroupId(@Param("groupId") Long groupId, Pageable pageable);
    @Query("""
    SELECT COUNT(mg) > 0 
    FROM MessageAudioGroup mg 
    WHERE mg.messageAudio.id = :messageId 
      AND mg.group.id IN (
        SELECT gur.group.id 
        FROM GroupUserRole gur 
        WHERE gur.user.id = :userId
      )
""")
    boolean isUserAuthorizedForMessage(@Param("userId") Long userId, @Param("messageId") Long messageId);

}
