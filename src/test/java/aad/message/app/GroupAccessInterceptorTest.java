package aad.message.app;

import aad.message.app.group.user.role.GroupUserRoleRepository;
import aad.message.app.middleware.GroupAccessInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class GroupAccessInterceptorTest {

    private GroupAccessInterceptor interceptor;

    @Mock
    private GroupUserRoleRepository groupUserRoleRepository;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        interceptor = new GroupAccessInterceptor(groupUserRoleRepository);
    }

    @Test
    public void testUserHasAccessToGroup() throws Exception {
        Long groupId = 1L;
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);
        when(groupUserRoleRepository.existsByUserIdAndGroupId(userId, groupId)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/groups/group/1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    public void testUserDoesNotHaveAccessToGroup() throws Exception {
        Long groupId = 1L;
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);
        when(groupUserRoleRepository.existsByUserIdAndGroupId(userId, groupId)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/groups/group/1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testUnauthorizedRequest() throws Exception {
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/invalid/path");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }
}
