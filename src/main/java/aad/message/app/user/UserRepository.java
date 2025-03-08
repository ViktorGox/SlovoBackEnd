package aad.message.app.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username); // TODO: Change to Optional<User> ?
    User findById(long id); // TODO: Change to Optional<User> ?
    Optional<User> findByUsernameOrEmail(String username, String email);
}
