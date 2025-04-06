package aad.message.app;

import aad.message.app.group.GroupRepository;
import aad.message.app.group.GroupService;
import aad.message.app.message.MessageRepository;
import aad.message.app.middleware.FileAccessInterceptor;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileAccessInterceptorTest {

    private FileAccessInterceptor interceptor;

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private GroupService groupService;
    @Mock private Authentication authentication;

    private final Long userId = 1L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        interceptor = new FileAccessInterceptor(userRepository, groupRepository, messageRepository, groupService);
    }

    @Test
    public void testAllowDefaultGroupImage() throws Exception {
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/files/images/gp_default.png");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    public void testAllowDefaultProfileImage() throws Exception {
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/files/images/pf_default.png");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    public void testAccessDeniedToGroupImage() throws Exception {
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(groupService.isUserInGroup(anyLong(), eq(userId))).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/files/images/gp-1-1-1-1-101.png");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testAccessAllowedToGroupImage() throws Exception {
        User user = new User();
        user.id = userId;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupService.isUserInGroup(1L, userId)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/files/images/gp-1-1-1-1-101.png");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    public void testAccessDeniedToProfileImage() throws Exception {
        User user = new User();
        user.id = userId;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupRepository.doesUserShareGroup(userId, 2L)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/files/images/pf-1-1-1-1-102.png");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testAccessAllowedToAudioMessage() throws Exception {
        User user = new User();
        user.id = userId;
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageRepository.isUserAuthorizedForMessage(userId, 10L)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/files/audio/ma-1-1-1-1-1010.mp3");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    public void testAccessDeniedToAudioMessage() throws Exception {
        when(authentication.getPrincipal()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(messageRepository.isUserAuthorizedForMessage(userId, 10L)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/files/audio/ma-1-1-1-1-1010.mp3");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(403, response.getStatus());
        assertEquals("User is not authorized to access this audio message", response.getContentAsString());
    }
}
