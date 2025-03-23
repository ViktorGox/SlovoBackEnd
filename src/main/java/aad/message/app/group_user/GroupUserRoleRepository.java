package aad.message.app.group_user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupUserRoleRepository extends JpaRepository<GroupUserRole, Long> {
    List<GroupUserRole> findByGroupId(Long groupId);
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    List<GroupUserRole> findByUserId(Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}