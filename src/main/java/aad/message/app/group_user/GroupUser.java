package aad.message.app.group_user;

import aad.message.app.group.Group;
import aad.message.app.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "group_user_role")
public class GroupUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    public Group group;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    public GroupUser() {}

    public GroupUser(Group group, User user) {
        this.group = group;
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}