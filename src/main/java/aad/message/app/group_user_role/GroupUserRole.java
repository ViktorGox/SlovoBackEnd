package aad.message.app.group_user_role;

import aad.message.app.group.Group;
import aad.message.app.role.Role;
import aad.message.app.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "group_user_role")
public class GroupUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    public Group group;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    public Role role;

    public GroupUserRole() {
    }

    public GroupUserRole(Group group, User user, Role role) {
        this.group = group;
        this.user = user;
        this.role = role;
    }
}