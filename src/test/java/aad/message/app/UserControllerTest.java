package aad.message.app;

import aad.message.app.group.GroupRepository;
import aad.message.app.group.GroupService;
import aad.message.app.jwt.JwtUtils;
import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.group.user.role.GroupUserRoleRepository;
import aad.message.app.message.MessageRepository;
import aad.message.app.refresh.token.RefreshTokenService;
import aad.message.app.acess.token.AccessTokenService;
import aad.message.app.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MessageRepository messageRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    GroupRepository groupRepository;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private FileUploadHandler fileUploadHandler;

    @MockitoBean
    private GroupUserRoleRepository groupUserRoleRepository;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private AccessTokenService accessTokenService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private GroupService groupService;

    @InjectMocks
    private UserController userController;

    private UserRegisterDTO userRegisterDTO;


    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testRegisterUser_passwordIsHashed() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.username = "testUser2";
        dto.password = "plainPassword123";
        dto.firstName = "John";
        dto.lastName = "Doe";
        dto.email = "testuser2@example.com";

        // Mock repository behavior
        User user = new User();
        user.username = dto.username;
        user.password = dto.password;
        user.firstName = dto.firstName;
        user.lastName = dto.lastName;
        user.email = dto.email;

        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0); // Get the argument passed to the save method
            savedUser.id = 1L; // Mock an ID after saving
            return savedUser; // Return the saved user
        });

        String mockAccessToken = "mockAccessToken";
        Mockito.when(jwtUtils.generateAccessToken(any(User.class))).thenReturn(mockAccessToken);

        // Mock jwtUtils.generateRefreshToken()
        String mockRefreshToken = "mockRefreshToken";
        Mockito.when(jwtUtils.generateRefreshToken(any(User.class))).thenReturn(mockRefreshToken);


        // Convert DTO to JSON string
        String dtoJson = new ObjectMapper().writeValueAsString(dto);

        // Use MockPart instead of MockMultipartFile
        MockPart dtoPart = new MockPart("dto", dtoJson.getBytes());
        dtoPart.getHeaders().setContentType(MediaType.valueOf("application/json")); // Important for correct parsing

        mockMvc.perform(multipart("/users")
                        .part(dtoPart)
                        .contentType("multipart/form-data")
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(captor.capture()); // Verify that save was called and capture the argument
        User savedUser = captor.getValue();  // Get the captured user

        // Verify that the JWT methods were called
        Mockito.verify(jwtUtils).generateAccessToken(savedUser);
        Mockito.verify(jwtUtils).generateRefreshToken(savedUser);

        // Assert that the password is hashed
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(dto.password, savedUser.password), "Password should be hashed");


        // Additional assertions for tokens (optional)
        assertEquals(mockAccessToken, jwtUtils.generateAccessToken(savedUser));
        assertEquals(mockRefreshToken, jwtUtils.generateRefreshToken(savedUser));
    }
}
