package aad.message.app.message.messagetext;

import aad.message.app.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageTextRepository extends JpaRepository<MessageText, Long> {
    List<MessageText> findByGroupId(Long groupId);

    Optional<MessageText> findTopByGroupIdOrderBySentDateDesc(Long groupId);

    Optional<MessageText> findTopByUserIdAndGroupIdOrderBySentDateDesc(Long userId, Long groupId);
    void deleteByGroup(Group group);

}
