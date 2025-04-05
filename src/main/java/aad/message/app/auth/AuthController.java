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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UserRepository repository;

    public AuthController(UserRepository repository, JwtUtils jwtUtils) {
        this.repository = repository;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthPostDTO dto) {
        Optional<User> user = repository.findByUsername(dto.username);

        if (user.isEmpty() || dto.password == null || dto.password.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username or password"));
        }

        var passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(dto.password, user.get().password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username or password"));
        }

        String accessToken = jwtUtils.generateAccessToken(user.get());
        String refreshToken = jwtUtils.generateRefreshToken(user.get());

        // Return both tokens in the response
        return ResponseEntity.ok()
                .body(Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.replace("Bearer ", "");

        Long userId = jwtUtils.validateTokenAndGetId(refreshToken, "refresh");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Refresh token is expired, invalid, or doesn't exist in the database"));
        }

        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "User not found"));
        }

        String newAccessToken = jwtUtils.generateAccessToken(user.get());
        String newRefreshToken  = jwtUtils.generateRefreshToken(user.get());

        return ResponseEntity.ok().body(Map.of(
                "refreshToken", newRefreshToken,
                "accessToken", newAccessToken
        ));
    }
}
