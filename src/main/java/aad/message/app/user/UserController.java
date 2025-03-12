package aad.message.app.user;

import aad.message.app.jwt.JwtUtils;
import aad.message.app.returns.Responses;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repository;
    private final ApplicationContext context;

    public UserController(UserRepository repository, ApplicationContext context) {
        this.repository = repository;
        this.context = context;
    }

    @GetMapping
    public ResponseEntity<?> getUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return repository.findById(userId)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDTO dto) {
        Collection<String> missingFields = UserRegisterDTO.verify(dto);
        if(!missingFields.isEmpty()) return Responses.IncompleteBody(missingFields);

        UserService userService = context.getBean(UserService.class);
        boolean isUnique = userService.isUserUnique(dto);

        if (!isUnique) {
            return Responses.Error("Either the email or username is already in use.");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = new User();
        user.username = dto.username;
        user.firstName = dto.firstName;
        user.lastName = dto.lastName;
        user.password = passwordEncoder.encode(dto.password);
        user.email = dto.email;

        user.imageUrl = "default.png"; // TODO: Set real image as default.

        User savedUser = repository.save(user);
        return Responses.Ok("token", JwtUtils.generateToken(savedUser.id));
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody UserUpdateDTO dto) {
        Collection<String> missingFields = UserUpdateDTO.verify(dto);
        if(!missingFields.isEmpty()) return Responses.IncompleteBody(missingFields);

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> user = repository.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error",
                    "User with id " + userId + " not found"));
        }

        if (dto.firstName != null) user.get().firstName = dto.firstName;
        if (dto.lastName != null) user.get().lastName = dto.lastName;
        if (dto.email != null) user.get().email = dto.email;

        repository.save(user.get());
        return ResponseEntity.ok().body(new UserDTO(user.get()));
    }
}
