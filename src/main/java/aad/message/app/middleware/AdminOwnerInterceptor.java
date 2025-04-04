package aad.message.app.middleware;

import aad.message.app.group.user.role.GroupUserRole;
import aad.message.app.group.user.role.GroupUserRoleRepository;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Optional;

@Component
public class AdminOwnerInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final GroupUserRoleRepository groupUserRoleRepository;

    public AdminOwnerInterceptor(UserRepository userRepository, GroupUserRoleRepository groupUserRoleRepository) {
        this.userRepository = userRepository;
        this.groupUserRoleRepository = groupUserRoleRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String groupIdStr = pathVariables.getOrDefault("id", pathVariables.get("group_id"));

        if (groupIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid group ID");
            return false;
        }

        Long groupId;
        try {
            groupId = Long.parseLong(groupIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid group ID format");
            return false;
        }

        Optional<GroupUserRole> groupUserRoleOptional = groupUserRoleRepository.findByUserIdAndGroupId(userId, groupId);

        if (groupUserRoleOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: No role found.");
            return false;
        }

        String role = groupUserRoleOptional.get().role.name;

        if (!"Owner".equals(role) && !"Admin".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: You must be an Admin or Owner.");
            return false;
        }

        return true;
    }
}
