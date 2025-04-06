package aad.message.app;

import aad.message.app.group.user.role.GroupUserRole;
import aad.message.app.group.user.role.GroupUserRoleRepository;
import aad.message.app.middleware.AdminOwnerInterceptor;
import aad.message.app.role.Role;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AdminOwnerInterceptorTest {

    private AdminOwnerInterceptor interceptor;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupUserRoleRepository groupUserRoleRepository;

    @Mock
    private Authentication authentication;

    private User currentUser;
    private GroupUserRole groupUserRole;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        currentUser = new User();
        currentUser.id = 1L;
        currentUser.username = "testUser";

        groupUserRole = new GroupUserRole();
        groupUserRole.role = new Role();
        groupUserRole.role.name = "Admin";

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser.id);

        interceptor = new AdminOwnerInterceptor(userRepository, groupUserRoleRepository);
    }

    @Test
    public void testAdminOrOwnerRoleAllowed() throws Exception {
        when(userRepository.findById(currentUser.id)).thenReturn(Optional.of(currentUser));
        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, 1L))
                .thenReturn(Optional.of(groupUserRole));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "1");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "1");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    public void testNonAdminOrOwnerRoleDenied() throws Exception {
        when(userRepository.findById(currentUser.id)).thenReturn(Optional.of(currentUser));

        GroupUserRole nonAdminRole = new GroupUserRole();
        nonAdminRole.role = new Role();
        nonAdminRole.role.name = "User";
        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, 1L))
                .thenReturn(Optional.of(nonAdminRole));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "1");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "1");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testUserNotFound() throws Exception {
        when(userRepository.findById(currentUser.id)).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "1");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "1");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    public void testInvalidGroupIdFormat() throws Exception {
        when(userRepository.findById(currentUser.id)).thenReturn(Optional.of(currentUser));

        GroupUserRole adminRoleForGroup = new GroupUserRole();
        adminRoleForGroup.role = new Role();
        adminRoleForGroup.role.name = "Admin";
        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, 1L))
                .thenReturn(Optional.of(adminRoleForGroup));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "invalid-id");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "invalid-id");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testMissingGroupId() throws Exception {
        when(userRepository.findById(currentUser.id)).thenReturn(Optional.of(currentUser));

        GroupUserRole adminRoleForGroup = new GroupUserRole();
        adminRoleForGroup.role = new Role();
        adminRoleForGroup.role.name = "Admin";
        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, 1L))
                .thenReturn(Optional.of(adminRoleForGroup));

        MockHttpServletRequest request = new MockHttpServletRequest();

        Map<String, String> pathVariables = new HashMap<>();

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(400, response.getStatus());
    }

}
