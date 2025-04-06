package aad.message.app;

import aad.message.app.auth.AuthController;
import aad.message.app.jwt.JwtUtils;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository repository;

    @InjectMocks
    private AuthController authController;

    private User user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        user = new User();
        user.id = 1L;
        user.username = "testUser";
        user.password = "hashedPassword";
    }

    @Test
    public void testRefreshToken_generatesNewTokens() throws Exception {
        String oldRefreshToken = "oldRefreshToken";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        when(jwtUtils.validateTokenAndGetId(oldRefreshToken, "refresh")).thenReturn(user.id);
        when(repository.findById(user.id)).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtUtils.generateRefreshToken(user)).thenReturn(newRefreshToken);

        mockMvc.perform(post("/refresh-token")
                        .header("Authorization", "Bearer " + oldRefreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(newRefreshToken));
    }

    @Test
    public void testRefreshToken_invalidRefreshToken() throws Exception {
        String invalidRefreshToken = "invalidRefreshToken";

        when(jwtUtils.validateTokenAndGetId(invalidRefreshToken, "refresh")).thenReturn(null);

        mockMvc.perform(post("/refresh-token")
                        .header("Authorization", "Bearer " + invalidRefreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());  // Ensure the status is 401 Unauthorized
    }

}
