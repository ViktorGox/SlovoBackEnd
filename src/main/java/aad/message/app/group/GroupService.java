package aad.message.app.group;

import aad.message.app.group_user.GroupUser;
import aad.message.app.group_user.GroupUserRepository;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, GroupUserRepository groupUserRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupUserRepository = groupUserRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public Group createGroup(Group group) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Group savedGroup = groupRepository.save(group);

        GroupUser groupUser = new GroupUser(savedGroup, user);
        groupUserRepository.save(groupUser);

        return savedGroup;
    }

    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    public void addUserToGroup(User user, Group group) {
        GroupUser userGroup = new GroupUser();
        userGroup.user = user;
        userGroup.group = group;
        groupUserRepository.save(userGroup);
    }
}
