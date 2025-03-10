package aad.message.app.jwt;

import aad.message.app.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;

public class JwtUtils {
    private static final String SECRET_KEY = "my_super_secret_key_which_should_be_very_long";
    private static final long EXPIRATION = 86400000; // 1 day
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(Long id) {
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Long validateTokenAndGetId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public static void encodedIdMatches(Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!id.equals(user.id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
