package aad.message.app;

import aad.message.app.acess.token.AccessToken;
import aad.message.app.acess.token.AccessTokenRepository;
import aad.message.app.jwt.JwtUtils;
import aad.message.app.refresh.token.RefreshToken;
import aad.message.app.refresh.token.RefreshTokenRepository;
import aad.message.app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtUtilsTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private JwtUtils jwtUtils;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Mocking JwtUtils constructor if needed for injecting a mock secretKey
        jwtUtils = new JwtUtils("mySecretKeySoLongTHATitShouldWorkPlsWork", refreshTokenRepository, accessTokenRepository);
    }

    @Test
    public void testGenerateAccessToken_isHashed() {
        // Given
        User user = new User();
        user.id = 1L;

        when(accessTokenRepository.findByUser(user)).thenReturn(Optional.empty()); // No existing access token

        ArgumentCaptor<AccessToken> accessTokenCaptor = ArgumentCaptor.forClass(AccessToken.class);

        String generatedToken = jwtUtils.generateAccessToken(user);

        assertNotNull(generatedToken); // Ensure a token was returned

        // Verify that the accessTokenRepository.save() was called
        verify(accessTokenRepository).save(accessTokenCaptor.capture());

        // Get the captured AccessToken and verify its hashed access token
        AccessToken capturedAccessToken = accessTokenCaptor.getValue();
        assertTrue(passwordEncoder.matches(generatedToken, capturedAccessToken.token), "Hashed token should match the generated token");
    }

    @Test
    public void testGenerateRefreshToken_isHashed() {
        User user = new User();
        user.id = 1L;

        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        // When
        String generatedToken = jwtUtils.generateRefreshToken(user);

        assertNotNull(generatedToken); // Ensure a token was returned

        // Verify that the refreshTokenRepository.save() was called
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        // Get the captured RefreshToken and verify its hashed refresh token
        RefreshToken capturedRefreshToken = refreshTokenCaptor.getValue();
        assertTrue(passwordEncoder.matches(generatedToken, capturedRefreshToken.token), "Hashed refresh token should match the generated token");
    }
}
