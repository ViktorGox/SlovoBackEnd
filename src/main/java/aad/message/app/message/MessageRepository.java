package aad.message.app.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findMessageById(long id);

    @Query("""
                SELECT m FROM Message m 
                LEFT JOIN MessageText mt ON m.id = mt.id 
                LEFT JOIN MessageAudio ma ON m.id = ma.id 
                LEFT JOIN ma.groups g 
                WHERE mt.group.id = :groupId OR g.id = :groupId
                ORDER BY m.sentDate
            """)
    List<Message> getMessagesByGroupId(@Param("groupId") Long groupId);
}
