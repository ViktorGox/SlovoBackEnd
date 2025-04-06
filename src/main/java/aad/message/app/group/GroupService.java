package aad.message.app.group;

import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.group.user.role.GroupUserRole;
import aad.message.app.group.user.role.GroupUserRoleRepository;
import aad.message.app.message.Message;
import aad.message.app.message.MessageService;
import aad.message.app.message.RecentMessageDTO;
import aad.message.app.message.messageaudio.MessageAudio;
import aad.message.app.message.messageaudio.MessageAudioRepository;
import aad.message.app.message.messagetext.MessageText;
import aad.message.app.message.messagetext.MessageTextRepository;
import aad.message.app.role.Role;
import aad.message.app.role.RoleRepository;
import aad.message.app.user.User;
import aad.message.app.user.UserRepository;
import aad.message.app.group.user.role.UserRoleDTO;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupUserRoleRepository groupUserRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageService messageService;
    private final MessageAudioRepository messageAudioRepository;
    private final MessageTextRepository messageTextRepository;
    private final FileUploadHandler fileUploadHandler;

    public GroupService(GroupRepository groupRepository, GroupUserRoleRepository groupUserRoleRepository, UserRepository userRepository, RoleRepository roleRepository, MessageService messageService, MessageAudioRepository messageAudioRepository, MessageTextRepository messageTextRepository, FileUploadHandler fileUploadHandler) {
        this.groupRepository = groupRepository;
        this.groupUserRoleRepository = groupUserRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.messageService = messageService;
        this.messageAudioRepository = messageAudioRepository;
        this.messageTextRepository = messageTextRepository;
        this.fileUploadHandler = fileUploadHandler;
    }
    @Transactional
    public Group createGroup(CreateGroupDTO createGroupDTO) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role ownerRole = roleRepository.findByName("Owner")
                .orElseThrow(() -> new RuntimeException("Role 'Owner' not found"));

        Group group = new Group();
        group.name = createGroupDTO.name;
        group.reminderFrequency = createGroupDTO.reminderFrequency;
        group.reminderStart = createGroupDTO.reminderStart;
        group.imageUrl = "gp_default.png";
        Group savedGroup = groupRepository.save(group);

        GroupUserRole ownerGroupUserRole = new GroupUserRole(savedGroup, user, ownerRole);
        groupUserRoleRepository.save(ownerGroupUserRole);

        if (createGroupDTO.usersWithRoles != null) {
            for (UserRoleDTO userRoleDTO : createGroupDTO.usersWithRoles) {
                User groupUser = userRepository.findById(userRoleDTO.userId)
                        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userRoleDTO.userId));

                Role role = roleRepository.findByName(userRoleDTO.roleName)
                        .orElseThrow(() -> new RuntimeException("Role '" + userRoleDTO.roleName + "' not found"));

                GroupUserRole groupUserRole = new GroupUserRole(savedGroup, groupUser, role);
                groupUserRoleRepository.save(groupUserRole);
            }
        }

        return savedGroup;
    }

    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    public List<GroupUserRole> getUsersByGroupId(Long groupId) {
        return groupUserRoleRepository.findByGroupId(groupId);
    }

    public boolean doesUserExist(Long userId) {
        return userRepository.existsById(userId);
    }

    public boolean isUserInGroup(Long groupId, Long userId) {
        return groupUserRoleRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    public boolean doesUserShareGroup(Long userId, Long otherUserId) {
        return groupRepository.doesUserShareGroup(userId, otherUserId);
    }

    public void addUserToGroup(Long groupId, Long userId) {
        GroupUserRole groupUserRole = new GroupUserRole();
        groupUserRole.group = groupRepository.findById(groupId).orElseThrow();
        groupUserRole.user = userRepository.findById(userId).orElseThrow();
        groupUserRole.role = roleRepository.findByName("User").orElseThrow();

        groupUserRoleRepository.save(groupUserRole);
    }

    public Group updateGroup(Group group) {
        return groupRepository.save(group);
    }

    public List<RecentChatDTO> getRecentChatsForUser(Long userId) {
        List<Group> groups = groupUserRoleRepository.findByUserId(userId)
                .stream()
                .map(groupUser -> groupUser.group)
                .toList();

        if (groups.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecentChatDTO> recentChats = new ArrayList<>();

        for (Group group : groups) {
            RecentChatDTO recentChat = new RecentChatDTO();
            recentChat.groupId = group.id;
            recentChat.groupTitle = group.name;
            recentChat.groupImage = group.imageUrl;

            // Get the last message from the user in this group
            Optional<Message> lastUserMessage = messageService.getLatestMessageByUser(userId, group.id);
            recentChat.lastUserMessageTime = lastUserMessage.map(message -> message.sentDate).orElse(null);

            // Get the last message in the group, irrespective of the user
            Optional<Message> lastMessage = messageService.getLatestMessageForGroup(group.id);
            if(lastMessage.isPresent()) {
                RecentMessageDTO recentMessageDTO = new RecentMessageDTO();
                Message message = lastMessage.get();
                recentMessageDTO.username = message.user.username;
                recentMessageDTO.messageType = message.messageType;
                recentMessageDTO.lastMessageTime = message.sentDate;
                if(message instanceof MessageText){
                    recentMessageDTO.messageText = ((MessageText) message).text;
                } else {
                    recentMessageDTO.messageText = "Audio";
                }
                recentChat.lastMessage = recentMessageDTO;
            } else {
                recentChat.lastMessage = null;
            }

            recentChats.add(recentChat);
        }

        return recentChats;
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException("Group not found");
        }
        Group group = optionalGroup.get();

        messageTextRepository.deleteByGroup(group);

        List<MessageAudio> audioMessages = messageAudioRepository.findAllByGroups_Id(groupId);
        for (MessageAudio audio : audioMessages) {
            audio.groups.remove(group);

            if (audio.groups.isEmpty()) {
                messageAudioRepository.delete(audio);
            } else {
                messageAudioRepository.save(audio);
            }
        }

        // Step 3: Delete all user roles for the group
        groupUserRoleRepository.deleteAllByGroup(group);

        // Step 4: If the group has a custom image, delete it
        if (group.imageUrl != null && !group.imageUrl.equals("gp_default.png")) {
            fileUploadHandler.removeFile(group.imageUrl);
        }

        // Step 5: Finally, delete the group
        groupRepository.delete(group);
    }


}
