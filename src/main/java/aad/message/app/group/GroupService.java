package aad.message.app.group;

import aad.message.app.group_user.GroupUserRole;
import aad.message.app.group_user.GroupUserRoleRepository;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupUserRoleRepository groupUserRoleRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, GroupUserRoleRepository groupUserRoleRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupUserRoleRepository = groupUserRoleRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public Group createGroup(Group group) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Group savedGroup = groupRepository.save(group);

        GroupUserRole groupUserRole = new GroupUserRole(savedGroup, user, null);
        groupUserRoleRepository.save(groupUserRole);

        return savedGroup;
    }

    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    public List<GroupUserRole> getUsersByGroupId(Long groupId) {
        return groupUserRoleRepository.findByGroupId(groupId);
    }

    public boolean doesUserExist(Long userId) {
        return userRepository.existsById(userId);
    }

    public boolean isUserInGroup(Long groupId, Long userId) {
        return groupUserRoleRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    public void addUserToGroup(Long groupId, Long userId) {
        GroupUserRole groupUserRole = new GroupUserRole();
        groupUserRole.group = groupRepository.findById(groupId).orElseThrow();
        groupUserRole.user = userRepository.findById(userId).orElseThrow();

        groupUserRoleRepository.save(groupUserRole);
    }

    public Group updateGroup(Group group) {
        return groupRepository.save(group);
    }
}
