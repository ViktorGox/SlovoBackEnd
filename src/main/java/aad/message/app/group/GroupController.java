package aad.message.app.group;

import aad.message.app.returns.Responses;
import aad.message.app.group_user.GroupUser;
import aad.message.app.user.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
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
            List<GroupUser> usersInGroup = groupService.getUsersByGroupId(id);
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

    @PutMapping("/{id}/name")
    public ResponseEntity<?> updateGroupName(@PathVariable Long id, @RequestBody String newName) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(id);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Group group = groupOptional.get();

            if (newName == null || newName.trim().isEmpty()) {
                return Responses.error("Group name cannot be empty.");
            }

            group.name = newName;

            groupService.updateGroup(group);

            return ResponseEntity.ok(GroupDTO.fromEntity(group));
        } catch (Exception e) {
            return Responses.internalError("An error occurred while updating the group name.");
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<?> updateGroupImage(@PathVariable Long id, @RequestBody String newImageUrl) {
        try {
            Optional<Group> groupOptional = groupService.getGroupById(id);
            if (groupOptional.isEmpty()) {
                return Responses.notFound("Group not found.");
            }

            Group group = groupOptional.get();

            if (newImageUrl == null || newImageUrl.trim().isEmpty()) {
                return Responses.error("Image URL cannot be empty.");
            }

            group.imageUrl = newImageUrl;

            groupService.updateGroup(group);

            return ResponseEntity.ok(GroupDTO.fromEntity(group));
        } catch (Exception e) {
            return Responses.internalError("An error occurred while updating the image URL.");
        }
    }
}