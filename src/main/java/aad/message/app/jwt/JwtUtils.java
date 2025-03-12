package aad.message.app.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

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

    // TODO: Throws an error when invalid, which is caught and returns 401, but is printed to the console.
    //  Gives expiredJwtException
    public static Long validateTokenAndGetId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
