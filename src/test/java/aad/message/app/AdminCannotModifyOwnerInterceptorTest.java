package aad.message.app;

import aad.message.app.group.Group;
import aad.message.app.group.user.role.GroupUserRole;
import aad.message.app.group.user.role.GroupUserRoleRepository;
import aad.message.app.middleware.AdminCannotModifyOwnerInterceptor;
import aad.message.app.role.Role;
import aad.message.app.user.User;
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

public class AdminCannotModifyOwnerInterceptorTest {

    private AdminCannotModifyOwnerInterceptor interceptor;

    @Mock
    private GroupUserRoleRepository groupUserRoleRepository;

    @Mock
    private Authentication authentication;

    private User currentUser;
    private User targetUser;
    private Role adminRole;
    private Role ownerRole;
    private Group group;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create users
        currentUser = new User();
        currentUser.id = 1L;
        currentUser.username = "adminUser";

        targetUser = new User();
        targetUser.id = 2L;
        targetUser.username = "ownerUser";

        // Create roles
        adminRole = new Role();
        adminRole.name = "Admin";

        ownerRole = new Role();
        ownerRole.name = "Owner";

        // Create group
        group = new Group();
        group.id = 1L;

        // Mock Authentication
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser.id);

        interceptor = new AdminCannotModifyOwnerInterceptor(groupUserRoleRepository);
    }

    @Test
    public void testAdminCannotModifyOwner() throws Exception {
        GroupUserRole adminRoleForGroup = new GroupUserRole(group, currentUser, adminRole);
        GroupUserRole ownerRoleForGroup = new GroupUserRole(group, targetUser, ownerRole);

        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, group.id))
                .thenReturn(Optional.of(adminRoleForGroup));
        when(groupUserRoleRepository.findByUserIdAndGroupId(targetUser.id, group.id))
                .thenReturn(Optional.of(ownerRoleForGroup));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "1");
        request.addParameter("user_id", "2");

        // Set path variables manually for the mock request
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "1");
        pathVariables.put("user_id", "2");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the modification
        assertEquals(403, response.getStatus()); // Forbidden
    }

    @Test
    public void testAdminCanModifyNonOwner() throws Exception {
        GroupUserRole adminRoleForGroup = new GroupUserRole(group, currentUser, adminRole);
        Role userRole = new Role();
        userRole.name = "User";
        GroupUserRole userRoleForGroup = new GroupUserRole(group, targetUser, userRole);

        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, group.id))
                .thenReturn(Optional.of(adminRoleForGroup));
        when(groupUserRoleRepository.findByUserIdAndGroupId(targetUser.id, group.id))
                .thenReturn(Optional.of(userRoleForGroup));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "1");
        request.addParameter("user_id", "2");

        // Set path variables manually for the mock request
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "1");
        pathVariables.put("user_id", "2");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        // Assert
        assertTrue(result); // It should allow the modification
    }

    @Test
    public void testMissingGroupId() throws Exception {
        GroupUserRole adminRoleForGroup = new GroupUserRole(group, currentUser, adminRole);

        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, group.id))
                .thenReturn(Optional.of(adminRoleForGroup));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("user_id", "2");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("user_id", "2");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testInvalidGroupIdFormat() throws Exception {
        GroupUserRole adminRoleForGroup = new GroupUserRole(group, currentUser, adminRole);

        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, group.id))
                .thenReturn(Optional.of(adminRoleForGroup));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "invalid-id");
        request.addParameter("user_id", "2");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "invalid-id");
        pathVariables.put("user_id", "2");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testMissingUserId() throws Exception {
        GroupUserRole adminRoleForGroup = new GroupUserRole(group, currentUser, adminRole);

        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, group.id))
                .thenReturn(Optional.of(adminRoleForGroup));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "1");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "1");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testInvalidUserIdFormat() throws Exception {
        GroupUserRole adminRoleForGroup = new GroupUserRole(group, currentUser, adminRole);

        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, group.id))
                .thenReturn(Optional.of(adminRoleForGroup));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "1");
        request.addParameter("user_id", "invalid-id");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "1");
        pathVariables.put("user_id", "invalid-id");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(400, response.getStatus()); // Bad Request
    }

    @Test
    public void testUserRoleNotFound() throws Exception {
        when(groupUserRoleRepository.findByUserIdAndGroupId(currentUser.id, group.id))
                .thenReturn(Optional.empty());
        when(groupUserRoleRepository.findByUserIdAndGroupId(targetUser.id, group.id))
                .thenReturn(Optional.of(new GroupUserRole(group, targetUser, ownerRole)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("group_id", "1");
        request.addParameter("user_id", "2");

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("group_id", "1");
        pathVariables.put("user_id", "2");

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result); // It should block the request
        assertEquals(403, response.getStatus()); // Forbidden
    }


}
