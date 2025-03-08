package aad.message.app.user;

import aad.message.app.jwt.JwtUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

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
    public List<User> getUsers() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User userPrincipal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!id.equals(userPrincipal.id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return repository.findById(id)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        UserService userService = context.getBean(UserService.class);
        boolean isUnique = userService.isUserUnique(dto);

        if (!isUnique) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error",
                    "Either the email or username is already in use."));
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
        // TODO: Return the user as well?
        return ResponseEntity.badRequest().body(Collections.singletonMap("token", JwtUtils.generateToken(savedUser.id)));
    }
}
