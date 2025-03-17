package aad.message.app.group;

import aad.message.app.group_user.GroupUser;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "\"group\"")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    public String imageUrl;

    @Column(name = "reminder_start")
    public java.time.LocalDateTime reminderStart;

    @Column(name = "reminder_frequency")
    public int reminderFrequency;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupUser> groupUsers;

}
