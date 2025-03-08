package aad.message.app.user;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    public User loadUserById(long id) {
        User user = userRepository.findById(id);
        if(user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    public boolean isUserUnique(User user) {
        Optional<User> existingUser = userRepository.findByUsernameOrEmail(user.username, user.email);
        return existingUser.isEmpty();
    }
}