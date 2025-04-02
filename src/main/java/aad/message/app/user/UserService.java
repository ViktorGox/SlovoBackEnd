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
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return user.get();
    }

    public Optional<User> loadUserById(long id) {
        return userRepository.findById(id);
    }

    public String checkUserUniqueness(UserRegisterDTO user) {
        Optional<User> existingUser = userRepository.findByUsernameOrEmail(user.username, user.email);

        if (existingUser.isPresent()) {
            User existing = existingUser.get();

            if (existing.username.equals(user.username)) {
                return "Username is already taken.";
            }
            if (existing.email.equals(user.email)) {
                return "Email is already registered.";
            }
        }

        return null; // No conflicts found
    }
}