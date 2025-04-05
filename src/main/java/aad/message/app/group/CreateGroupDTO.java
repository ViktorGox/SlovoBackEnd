package aad.message.app.group;

import aad.message.app.group.user.role.UserRoleDTO;

import java.time.LocalDateTime;
import java.util.List;

public class CreateGroupDTO {
    public String name;
    public LocalDateTime reminderStart;
    public Integer reminderFrequency;
    public List<UserRoleDTO> usersWithRoles;
}
