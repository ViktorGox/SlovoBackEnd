package aad.message.app.middleware;

import aad.message.app.group_user.GroupUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GroupAccessInterceptor implements HandlerInterceptor {
    private final GroupUserRepository groupUserRepository;

    public GroupAccessInterceptor(GroupUserRepository groupUserRepository) {
        this.groupUserRepository = groupUserRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // TODO: Doesn't handle post message/audio and message/text because they have group ids in the body.

        Pattern pattern = Pattern.compile("^/(groups|messages)/(\\d+)");
        Matcher matcher = pattern.matcher(request.getRequestURI());

        if (matcher.find()) {
            Long groupId = Long.parseLong(matcher.group(2));

            if (!groupUserRepository.existsByUserIdAndGroupId(userId, groupId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not a member of this group.");
                return false;
            }
        }

        return true;
    }
}
