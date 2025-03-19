package aad.message.app.auth;

import aad.message.app.jwt.JwtUtils;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;

@RestController
public class AuthController {

    private final UserRepository repository;

    public AuthController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthPostDTO dto) {
        // TODO: Check the body contains the required parts.
        Optional<User> user = repository.findByUsername(dto.username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username or password"));
        }

        var passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(dto.password, user.get().password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username or password"));
        }

        return ResponseEntity.ok().body(Collections.singletonMap("token", JwtUtils.generateToken(user.get().id)));
    }
}
