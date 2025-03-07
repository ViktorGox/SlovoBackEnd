package aad.message.app.user;

import aad.message.app.jwt.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<User> getUsers() {
        return repository.findAll();
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!id.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return repository.findById(id).orElseThrow();
    }

    @PostMapping
    public String register(@RequestBody User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        User savedUser = repository.save(user);
        return JwtUtils.generateToken(savedUser.getId());
    }
}

