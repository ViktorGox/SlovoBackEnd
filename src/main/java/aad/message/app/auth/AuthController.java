package aad.message.app.auth;

import aad.message.app.jwt.JwtUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // Verify user in DB...
        Long userId = 123L;
        return JwtUtils.generateToken(userId);
    }
}
