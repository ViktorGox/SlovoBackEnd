package aad.message.app.group;

import aad.message.app.filetransfer.FileType;
import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.returns.Responses;
import aad.message.app.group_user.GroupUserRole;
import aad.message.app.user.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;
    private final FileUploadHandler fileUploadHandler;

    public GroupController(GroupService groupService, FileUploadHandler fileUploadHandler) {
        this.groupService = groupService;
        this.fileUploadHandler = fileUploadHandler;
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
            List<GroupUserRole> usersInGroup = groupService.getUsersByGroupId(id);
            if (usersInGroup.isEmpty()) {
                return Responses.notFound("No users found for this group.");
            }
            List<UserDTO> userDTOs = usersInGroup.stream()
                    .map(groupUser -> new UserDTO(groupUser.user))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return Responses.internalError("An error occurred while fetching the users.");
        }
    }


    @PostMapping
    public ResponseEntity<?> createGroup(@Valid @RequestBody Group group) {
        if (group.name == null || group.name.trim().isEmpty()) {
            return Responses.error("Group name is required.");
        }

        try {
            Group createdGroup = groupService.createGroup(group);
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

            return ResponseEntity.ok().body("User added to the group successfully.");
        } catch (Exception e) {
            return Responses.internalError("An error occurred while adding the user to the group.");
        }
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<?> updateGroupName(@PathVariable Long id, @RequestBody GroupNameUpdateDTO dto) {
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
}