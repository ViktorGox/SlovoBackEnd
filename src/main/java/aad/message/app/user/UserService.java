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

    public boolean isUserUnique(UserRegisterDTO user) {
        Optional<User> existingUser = userRepository.findByUsernameOrEmail(user.username, user.email);
        return existingUser.isEmpty();
    }
}