package aad.message.app.middleware;

import aad.message.app.group.user.role.GroupUserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static aad.message.app.middleware.ResponseUtil.writeErrorResponse;

@Component
public class GroupAccessInterceptor implements HandlerInterceptor {
    private final GroupUserRoleRepository groupUserRoleRepository;

    public GroupAccessInterceptor(GroupUserRoleRepository groupUserRoleRepository) {
        this.groupUserRoleRepository = groupUserRoleRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Pattern pattern = Pattern.compile("^/(groups|messages)((?:/group)?)/(\\d+)");
        Matcher matcher = pattern.matcher(request.getRequestURI());

        if (matcher.find()) {
            Long groupId = Long.parseLong(matcher.group(3));

            if(isUnauthorizedForGroup(groupUserRoleRepository, List.of(groupId))) {
                writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "You are not a member of this group.");
                return false;
            }
            else {
                return true;
            }
        }
        writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized request.");
        return false;
    }

    public static boolean isUnauthorizedForGroup(GroupUserRoleRepository groupUserRoleRepository, List<Long> groupIds) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        for (Long id : groupIds) {
            if (!groupUserRoleRepository.existsByUserIdAndGroupId(userId, id)) {
                return true;
            }
        }
        return false;
    }
}
