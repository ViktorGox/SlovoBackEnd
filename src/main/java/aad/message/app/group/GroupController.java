package aad.message.app.group;

import aad.message.app.filetransfer.FileType;
import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.group.user.role.GroupUserRoleRepository;
import aad.message.app.message.Message;
import aad.message.app.message.MessageService;
import aad.message.app.returns.Responses;
import aad.message.app.group.user.role.GroupUserRole;
import aad.message.app.role.Role;
import aad.message.app.role.RoleService;
import aad.message.app.user.UserRoleMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;
    private final FileUploadHandler fileUploadHandler;
    private final GroupUserRoleRepository groupUserRoleRepository;
    private final RoleService roleService;
    private final MessageService messageService;

    public GroupController(GroupService groupService, FileUploadHandler fileUploadHandler, GroupUserRoleRepository groupUserRoleRepository, RoleService roleService, MessageService messageService) {
        this.groupService = groupService;
        this.fileUploadHandler = fileUploadHandler;
        this.groupUserRoleRepository = groupUserRoleRepository;
        this.roleService = roleService;
        this.messageService = messageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Long id) {
        try {
            Optional<Group> group = groupService.getGroupById(id);
            if (group.isEmpty()) {
                return Responses.notFound("Group not found.");
            }
            return ResponseEntity.ok(GroupDTO.fromEntity(group.get()));
        } catch (Exception e) {
            return Responses.internalError("An error occurred while fetching the group.");
        }
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<?> getUsersByGroupId(@PathVariable Long id) {
        try {
            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<GroupUserRole> usersInGroup = groupService.getUsersByGroupId(id);
            if (usersInGroup.isEmpty()) {
                return Responses.notFound("No users found for this group.");
            }

            List<UserRoleMessageDTO> userDTOs = usersInGroup.stream().map(groupUser -> {
                Optional<Message> latestMessage = messageService.getLatestMessageByUser(groupUser.user.id, id);
                LocalDateTime lastMessageTime = latestMessage.map(msg -> msg.sentDate).orElse(null);

                return new UserRoleMessageDTO(groupUser.user, groupUser.role, lastMessageTime);
            }).collect(Collectors.toList());

            // Ensure the requesting user appears first
            userDTOs.sort((u1, u2) -> u1.id.equals(userId) ? -1 : (u2.id.equals(userId) ? 1 : 0));

            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return Responses.internalError("An error occurred while fetching the users.");
        }
    }


    @GetMapping("/recentChats")
    public ResponseEntity<?> getRecentChats() {
        try {
            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            List<RecentChatDTO> recentChats = groupService.getRecentChatsForUser(userId);

            if (recentChats.isEmpty()) {
                return Responses.notFound("No groups found. Try to create a new one.");
            }

            return ResponseEntity.ok(recentChats);

        } catch (Exception e) {
            return Responses.internalError("An error occurred while fetching recent chats.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestPart(value = "dto", required = false) CreateGroupDTO createGroupDTO,
                                         @RequestPart(value = "file", required = false) MultipartFile file) {
        if (createGroupDTO == null || createGroupDTO.name == null || createGroupDTO.name.trim().isEmpty()) {
            return Responses.error("Field 'name' is missing");
        }

        try {
            Group createdGroup = groupService.createGroup(createGroupDTO);
            String imageUrl = "gp_default.png";

            if (file != null && !file.isEmpty()) {
                ResponseEntity<?> fileUploadResult = fileUploadHandler.uploadFile(file, FileType.GROUP_PICTURE, createdGroup.id);
                if (fileUploadResult.getStatusCode() != HttpStatus.OK) return fileUploadResult;

                imageUrl = fileUploadHandler.okFileName(fileUploadResult);
            }
            createdGroup.imageUrl = imageUrl;

            groupService.updateGroup(createdGroup);

            return ResponseEntity.ok(GroupDTO.fromEntity(createdGroup));
        } catch (Exception e) {
            return Responses.internalError("An error occurred while creating the group.");
        }
    }


    @PostMapping("/{groupId}/users/{userId}")
    public ResponseEntity<?> addUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(groupId);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            boolean userExists = groupService.doesUserExist(userId);
            if (!userExists) {
                return Responses.notFound("User not found.");
            }

            boolean alreadyInGroup = groupService.isUserInGroup(groupId, userId);
            if (alreadyInGroup) {
                return Responses.error("User is already in the group.");
            }

            groupService.addUserToGroup(groupId, userId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return Responses.internalError("An error occurred while adding the user to the group.");
        }
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<?> updateGroupName(@PathVariable Long id, @RequestBody(required = false) GroupNameUpdateDTO dto) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(id);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Group group = groupOptional.get();

            if (dto == null || dto.newName == null || dto.newName.trim().isEmpty()) {
                return Responses.error("Group name cannot be empty.");
            }

            group.name = dto.newName;

            groupService.updateGroup(group);

            return ResponseEntity.ok(GroupDTO.fromEntity(group));
        } catch (Exception e) {
            return Responses.internalError("An error occurred while updating the group name.");
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<?> updateGroupImage(@PathVariable Long id,
                                              @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(id);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Group group = groupOptional.get();

            ResponseEntity<?> fileUploadResult = fileUploadHandler.uploadFile(file, FileType.GROUP_PICTURE, group.id);
            if (fileUploadResult.getStatusCode() != HttpStatus.OK) return fileUploadResult;

            String fileName = fileUploadHandler.okFileName(fileUploadResult);
            if(!group.imageUrl.equals("gp_default.png")) {
                // do not remove the default image.
                fileUploadHandler.removeFile(group.imageUrl);
            }
            group.imageUrl = fileName;

            groupService.updateGroup(group);

            return ResponseEntity.ok(GroupDTO.fromEntity(group));
        } catch (Exception e) {
            return Responses.internalError("An error occurred while updating the image URL.");
        }
    }

    @PutMapping("/{group_id}/{user_id}/{role_id}")
    public ResponseEntity<?> updateUserRole(@PathVariable("group_id") Long groupId,
                                            @PathVariable("user_id") Long userId,
                                            @PathVariable("role_id") String roleName) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(groupId);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Optional<GroupUserRole> groupUserRoleOptional = groupUserRoleRepository.findByUserIdAndGroupId(userId, groupId);
            if (groupUserRoleOptional.isEmpty()) {
                return Responses.notFound("User not found in the group.");
            }

            GroupUserRole groupUserRole = groupUserRoleOptional.get();

            Optional<Role> roleOptional = roleService.getRoleByName(roleName);
            if (roleOptional.isEmpty()) {
                return Responses.notFound("Role not found.");
            }

            Role newRole = roleOptional.get();

            if (groupUserRole.role.equals(newRole)) {
                return Responses.error("User already has this role.");
            }

            groupUserRole.role = newRole;
            groupUserRoleRepository.save(groupUserRole);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return Responses.internalError("An error occurred while updating the user role.");
        }
    }

    @PutMapping("/{group_id}/reminder")
    public ResponseEntity<?> updateGroupReminder(
            @PathVariable("group_id") Long groupId,
            @RequestBody(required = false) ReminderUpdateDTO reminderUpdateDTO) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(groupId);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Group group = groupOptional.get();

            // If both values are null, it means reminders are disabled.
            group.reminderStart = reminderUpdateDTO.getReminderStart();
            group.reminderFrequency = reminderUpdateDTO.getReminderFrequency();

            groupService.updateGroup(group);

            return ResponseEntity.ok(GroupDTO.fromEntity(group));
        } catch (Exception e) {
            return Responses.internalError("An error occurred while updating the reminder settings.");
        }
    }

    @DeleteMapping("/{group_id}/{user_id}")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable("group_id") Long groupId,
                                                 @PathVariable("user_id") Long userId) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(groupId);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Optional<GroupUserRole> groupUserRoleOptional = groupUserRoleRepository.findByUserIdAndGroupId(userId, groupId);
            if (groupUserRoleOptional.isEmpty()) {
                return Responses.notFound("User not found in the group.");
            }

            groupUserRoleRepository.delete(groupUserRoleOptional.get());

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return Responses.internalError("An error occurred while removing the user from the group.");
        }
    }

    @DeleteMapping("/{group_id}/self")
    public ResponseEntity<?> removeSelfFromGroup(@PathVariable("group_id") Long groupId) {
        try {
            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Optional<Group> groupOptional = groupService.getGroupById(groupId);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Optional<GroupUserRole> groupUserRoleOptional = groupUserRoleRepository.findByUserIdAndGroupId(userId, groupId);
            if (groupUserRoleOptional.isEmpty()) {
                return Responses.notFound("You are not a member of this group.");
            }

            GroupUserRole groupUserRole = groupUserRoleOptional.get();

            if (groupUserRole.role != null && groupUserRole.role.name.equals("Owner")) {

                Optional<GroupUserRole> firstAdmin = groupUserRoleRepository.findFirstByGroupIdAndRoleName(groupId, "Admin");

                if (firstAdmin.isPresent()) {
                    GroupUserRole adminRole = firstAdmin.get();
                    adminRole.role = groupUserRole.role;
                    groupUserRoleRepository.save(adminRole);
                } else {
                    Optional<GroupUserRole> firstUser = groupUserRoleRepository.findFirstByGroupIdAndRoleName(groupId, "User");

                    if (firstUser.isPresent()) {
                        GroupUserRole userRole = firstUser.get();
                        userRole.role = groupUserRole.role;
                        groupUserRoleRepository.save(userRole);
                    }
                }
            }

            // Remove the user from the group
            groupUserRoleRepository.delete(groupUserRole);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return Responses.internalError("An error occurred while removing yourself from the group.");
        }
    }

    @DeleteMapping("/{group_id}")
    public ResponseEntity<?> deleteGroup(@PathVariable("group_id") Long groupId) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(groupId);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<GroupUserRole> groupUserRole = groupUserRoleRepository.findByUserIdAndGroupId(userId, groupId);
            if (groupUserRole.isEmpty() || !groupUserRole.get().role.name.equals("Owner")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not allowed to perform this action."));
            }

            groupService.deleteGroup(groupId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return Responses.internalError("An error occurred while deleting the group.");
        }
    }

}