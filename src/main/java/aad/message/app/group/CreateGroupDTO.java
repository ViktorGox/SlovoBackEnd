package aad.message.app.group;

import aad.message.app.group_user_role.UserRoleDTO;

import java.util.List;

public class CreateGroupDTO {
    public String name;
    public List<UserRoleDTO> usersWithRoles;
}
