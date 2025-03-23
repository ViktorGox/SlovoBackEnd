package aad.message.app.user;

import aad.message.app.filetransfer.ImageContainer;
import aad.message.app.group_user.GroupUserRole;
import jakarta.persistence.*;

import java.util.Set;

/**
 * User entity class for logic, not transfer.
 */
@Entity
@Table(name = "\"user\"")
public class User implements ImageContainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String username;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    public String email;
    public String password;

    @Column(name = "image_url")
    public String imageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
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
