package aad.message.app.group.user.role;

import aad.message.app.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupUserRoleRepository extends JpaRepository<GroupUserRole, Long> {
    List<GroupUserRole> findByGroupId(Long groupId);
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    List<GroupUserRole> findByUserId(Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    Optional<GroupUserRole> findByUserIdAndGroupId(Long userId, Long groupId);

    Optional<GroupUserRole> findFirstByGroupIdAndRoleName(Long groupId, String roleName);
    void deleteAllByGroup(Group group);
}