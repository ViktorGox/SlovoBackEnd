package aad.message.app.user;


import java.util.ArrayList;
import java.util.Collection;

/**
 * User model only for registering request.
 */
public class UserRegisterDTO {
    public String username;
    public String firstName;
    public String lastName;
    public String password;
    public String email;

    public static Collection<String> verify(UserRegisterDTO dto) {
        ArrayList<String> list = new ArrayList<>();

        if(dto.username == null) list.add("username");
        if(dto.firstName == null) list.add("firstName");
        // Lastname is okay to be empty.
        if(dto.password == null) list.add("password");
        if(dto.email == null) list.add("email");

        return list;
    }
}
