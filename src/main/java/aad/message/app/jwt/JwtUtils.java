package aad.message.app.jwt;

import aad.message.app.acess.token.AccessToken;
import aad.message.app.acess.token.AccessTokenRepository;
import aad.message.app.refresh_token.RefreshToken;
import aad.message.app.refresh_token.RefreshTokenRepository;
import aad.message.app.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final AccessTokenRepository accessTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    public JwtUtils(@Value("${jwt.secret}") String secretKey, RefreshTokenRepository refreshTokenRepository, AccessTokenRepository accessTokenRepository) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String generateAccessToken(User user) {
        Optional<AccessToken> existingAccessToken = accessTokenRepository.findByUser(user);
        existingAccessToken.ifPresent(accessTokenRepository::delete);

        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(user.id))
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String hashedAccessToken = passwordEncoder.encode(accessToken);

        Date accessTokenExpiryDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);
        AccessToken newAccessTokenObj = new AccessToken(hashedAccessToken, user, accessTokenExpiryDate);
        accessTokenRepository.save(newAccessTokenObj);

        return accessToken;
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

        String hashedRefreshToken = passwordEncoder.encode(refreshToken);

        Date refreshTokenExpiryDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);
        RefreshToken newRefreshTokenObj = new RefreshToken(hashedRefreshToken, user, refreshTokenExpiryDate);
        refreshTokenRepository.save(newRefreshTokenObj);

        return refreshToken;
    }

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

            Long userId = Long.parseLong(claims.getSubject());

            //If the token is a refresh token, check the database for its validity
            if (expectedType.equals("refresh")) {
                Optional<RefreshToken> storedRefreshToken = refreshTokenRepository.findByUserId(userId);

                if (storedRefreshToken.isEmpty() || !passwordEncoder.matches(token, storedRefreshToken.get().token)) {
                    return null; // Token does not exist in the DB or doesn't match
                }
            } else  if (expectedType.equals("access")) {
                Optional<AccessToken> storedAccessToken = accessTokenRepository.findByUserId(userId);

                if (storedAccessToken.isEmpty() || !passwordEncoder.matches(token, storedAccessToken.get().token)) {
                    return null; // Token does not exist in the DB or doesn't match
                }
            }

            return userId; // Token is valid, return the user ID
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException e) {
            return null; // Token is expired or tampered with
        }
    }
}
