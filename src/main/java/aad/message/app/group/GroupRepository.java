package aad.message.app.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findGroupById(long id);

    @Query("SELECT COUNT(ug) > 0 FROM GroupUserRole ug WHERE ug.user.id = :userId AND ug.group.id IN (SELECT ug2.group.id FROM GroupUserRole ug2 WHERE ug2.user.id = :otherUserId)")
    boolean doesUserShareGroup(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);
}