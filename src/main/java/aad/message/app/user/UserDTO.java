package aad.message.app.user;

/**
 * Used for outgoing requests.
 */
public class UserDTO {
    public Long id;
    public String username;
    public String firstName;
    public String lastName;
    public String email;
    public String imageUrl;

    public UserDTO(User user) {
        this.id = user.id;
        this.username = user.username;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.email = user.email;
        this.imageUrl = user.imageUrl;
    }
}
