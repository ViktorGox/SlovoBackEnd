package aad.message.app.group;

import aad.message.app.group_user.GroupUser;
import aad.message.app.group_user.GroupUserRepository;
import aad.message.app.user.User;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupUserRepository userGroupRepository;

    public GroupService(GroupRepository groupRepository, GroupUserRepository userGroupRepository) {
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
    }

    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    public void addUserToGroup(User user, Group group) {
        GroupUser userGroup = new GroupUser();
        userGroup.user = user;
        userGroup.group = group;
        userGroupRepository.save(userGroup);
    }
}
