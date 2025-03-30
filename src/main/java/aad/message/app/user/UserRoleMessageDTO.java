package aad.message.app.user;

import aad.message.app.role.Role;

import java.time.LocalDateTime;

public class UserRoleMessageDTO {
    public Long id;
    public String username;
    public String firstName;
    public String lastName;
    public String imageUrl;
    public Role role;
    public LocalDateTime lastMessageTime;

    public UserRoleMessageDTO(User user, Role role, LocalDateTime lastMessageTime) {
        this.id = user.id;
        this.username = user.username;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.imageUrl = user.imageUrl;
        this.role = role;
        this.lastMessageTime = lastMessageTime;
    }
}