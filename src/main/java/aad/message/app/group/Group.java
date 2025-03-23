package aad.message.app.group;

import aad.message.app.filetransfer.ImageContainer;
import aad.message.app.group_user_role.GroupUserRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "\"group\"")
public class Group implements ImageContainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    public String imageUrl;

    @Column(name = "reminder_start")
    public LocalDateTime reminderStart;

    @Column(name = "reminder_frequency")
    public Integer reminderFrequency;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupUserRole> groupUserRoles;

    @Override
    public String getImageURL() {
        return imageUrl;
    }

    @Override
    public void SetImageURL(String imageURL) {
        imageUrl = imageURL;
    }
}
