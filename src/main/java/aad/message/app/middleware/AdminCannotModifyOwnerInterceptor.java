package aad.message.app.middleware;

import aad.message.app.group.user.role.GroupUserRole;
import aad.message.app.group.user.role.GroupUserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

@Component
public class AdminCannotModifyOwnerInterceptor implements HandlerInterceptor {

    private final GroupUserRoleRepository groupUserRoleRepository;

    public AdminCannotModifyOwnerInterceptor(GroupUserRoleRepository groupUserRoleRepository) {
        this.groupUserRoleRepository = groupUserRoleRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String groupIdStr = pathVariables.getOrDefault("id", pathVariables.get("group_id"));
        if (groupIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing group ID");
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

        String targetUserIdStr = pathVariables.get("user_id");
        if (targetUserIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing target user ID");
            return false;
        }

        Long targetUserId;
        try {
            targetUserId = Long.parseLong(targetUserIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid target user ID format");
            return false;
        }

        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<GroupUserRole> targetUserRole = groupUserRoleRepository.findByUserIdAndGroupId(targetUserId, groupId);
        Optional<GroupUserRole> currentUserRole = groupUserRoleRepository.findByUserIdAndGroupId(currentUserId, groupId);

        // If either role is missing, deny access
        if (targetUserRole.isEmpty() || currentUserRole.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("User role not found in group");
            return false;
        }

        // If the current user is an Admin and trying to modify an Owner, block it
        if (currentUserRole.get().role.name.equalsIgnoreCase("Admin") &&
                targetUserRole.get().role.name.equalsIgnoreCase("Owner")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Admins cannot modify Owners");
            return false;
        }

        return true;
    }
}
