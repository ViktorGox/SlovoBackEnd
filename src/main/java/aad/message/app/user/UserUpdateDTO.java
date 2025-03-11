package aad.message.app.user;

import java.util.ArrayList;
import java.util.Collection;

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

    public static Collection<String> verify(UserUpdateDTO dto) {
        ArrayList<String> list = new ArrayList<>();

        if(dto.firstName == null) list.add("firstName");
        // Lastname is okay to be empty.
        if(dto.email == null) list.add("email");

        return list;
    }
}
