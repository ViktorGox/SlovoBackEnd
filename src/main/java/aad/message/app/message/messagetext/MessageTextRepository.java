package aad.message.app.message.messagetext;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageTextRepository extends JpaRepository<MessageText, Long> {
    List<MessageText> findByGroupId(Long groupId);
}
