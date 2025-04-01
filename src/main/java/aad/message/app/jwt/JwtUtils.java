package aad.message.app.jwt;

import aad.message.app.refresh_token.RefreshToken;
import aad.message.app.refresh_token.RefreshTokenRepository;
import aad.message.app.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class JwtUtils {
    private static final long ACCESS_TOKEN_EXPIRATION = 900000; // 15 min
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days
    private final Key key;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtUtils(@Value("${jwt.secret}") String secretKey, RefreshTokenRepository refreshTokenRepository) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUser(user);
        existingRefreshToken.ifPresent(refreshTokenRepository::delete);

        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(user.id))
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Date refreshTokenExpiryDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);
        RefreshToken newRefreshTokenObj = new RefreshToken(refreshToken, user, refreshTokenExpiryDate);
        refreshTokenRepository.save(newRefreshTokenObj);

        return refreshToken;
    }

    // TODO: Throws an error when invalid, which is caught and returns 401, but is printed to the console.
    //  Gives expiredJwtException
    //  Gives SignatureException if signature doesn't match.
    public Long validateTokenAndGetId(String token, String expectedType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenType = claims.get("type", String.class);
            if (tokenType == null || !tokenType.equals(expectedType)) {
                return null; // Invalid type, should return 401
            }

            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException e) {
            return null; // Token is expired or tampered with
        }
    }
}
