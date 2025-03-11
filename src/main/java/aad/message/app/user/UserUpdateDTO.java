package aad.message.app.user;

public class UserUpdateDTO {
    public String firstName;
    public String lastName;
    public String email;

    public UserUpdateDTO() {}

    public UserUpdateDTO(User user) {
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.email = user.email;
    }
}
