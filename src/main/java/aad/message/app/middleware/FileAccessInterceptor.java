package aad.message.app.middleware;

import aad.message.app.filetransfer.FileType;
import aad.message.app.group.GroupService;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import aad.message.app.group.GroupRepository;
import aad.message.app.message.MessageRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

@Component
public class FileAccessInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;
    private final GroupService groupService;

    public FileAccessInterceptor(UserRepository userRepository, GroupRepository groupRepository, MessageRepository messageRepository, GroupService groupService) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.messageRepository = messageRepository;
        this.groupService = groupService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        Long id = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (id == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false;
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not found");
            return false;
        }

        User user = userOpt.get();

        if (requestURI.contains("/files/images/")) {
            String fileName = requestURI.substring(requestURI.lastIndexOf("/") + 1);
            FileType fileType = determineFileType(fileName);

            if (fileName.equals("gp_default.png") || fileName.equals("pf_default.png")) {
                return true;
            }

            if (fileType == FileType.GROUP_PICTURE) {
                Long groupId = extractGroupIdFromFileName(fileName);
                if (!isUserPartOfGroup(user, groupId)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("User is not part of this group");
                    return false;
                }
            } else if (fileType == FileType.PROFILE_PICTURE) {
                Long userId = extractUserIdFromFileName(fileName);
                if (!isUserProfileImageAccessible(user, userId)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("User cannot access this profile image");
                    return false;
                }
            }
        } else if (requestURI.contains("/files/audio/")) {
            String fileName = requestURI.substring(requestURI.lastIndexOf("/") + 1);
            Long messageId = extractMessageIdFromFileName(fileName);

            if (!isUserAuthorizedForMessage(user, messageId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("User is not authorized to access this audio message");
                return false;
            }
        }

        return true;
    }

    private boolean isUserPartOfGroup(User user, Long groupId) {
        return groupService.isUserInGroup(groupId, user.id);
    }

    private boolean isUserProfileImageAccessible(User user, Long userId) {
        if (user.id.equals(userId)) {
            return true;
        }

        return groupRepository.doesUserShareGroup(user.id, userId);
    }

    private boolean isUserAuthorizedForMessage(User user, Long messageId) {
        return messageRepository.isUserAuthorizedForMessage(user.id, messageId);
    }

    private FileType determineFileType(String fileName) {
        if (fileName.contains(FileType.GROUP_PICTURE.getShortName())) {
            return FileType.GROUP_PICTURE;
        } else if (fileName.contains(FileType.PROFILE_PICTURE.getShortName())) {
            return FileType.PROFILE_PICTURE;
        } else if (fileName.contains(FileType.MESSAGE_AUDIO.getShortName())) {
            return FileType.MESSAGE_AUDIO;
        }
        return null;
    }

    private Long extractGroupIdFromFileName(String fileName) {
        String[] parts = fileName.split("-");
        String value = parts[5].substring(2); // remove first 2 characters
        int dotIndex = value.indexOf('.');    // find index of the first dot

        if (dotIndex != -1) {
            value = value.substring(0, dotIndex); // take up to the dot
        }

        return Long.parseLong(value);
    }

    private Long extractUserIdFromFileName(String fileName) {
        String[] parts = fileName.split("-");
        String value = parts[5].substring(2); // remove first 2 characters
        int dotIndex = value.indexOf('.');    // find index of the first dot

        if (dotIndex != -1) {
            value = value.substring(0, dotIndex); // take up to the dot
        }

        return Long.parseLong(value);
    }

    private Long extractMessageIdFromFileName(String fileName) {
        String[] parts = fileName.split("-");
        String value = parts[5].substring(2); // remove first 2 characters
        int dotIndex = value.indexOf('.');    // find index of the first dot

        if (dotIndex != -1) {
            value = value.substring(0, dotIndex); // take up to the dot
        }

        return Long.parseLong(value);
    }
}
