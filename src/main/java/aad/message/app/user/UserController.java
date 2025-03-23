package aad.message.app.user;

import aad.message.app.filetransfer.FileType;
import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.group.Group;
import aad.message.app.group.GroupDTO;
import aad.message.app.group_user_role.GroupUserRoleRepository;
import aad.message.app.jwt.JwtUtils;
import aad.message.app.returns.Responses;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    private final JwtUtils jwtUtils;
    private final UserRepository repository;
    private final ApplicationContext context;
    private final FileUploadHandler fileUploadHandler;
    private final GroupUserRoleRepository groupUserRoleRepository;

    public UserController(UserRepository repository,
                          ApplicationContext context,
                          FileUploadHandler fileUploadHandler,
                          JwtUtils jwtUtils,
                          GroupUserRoleRepository groupUserRoleRepository) {
        this.repository = repository;
        this.context = context;
        this.fileUploadHandler = fileUploadHandler;
        this.jwtUtils = jwtUtils;
        this.groupUserRoleRepository = groupUserRoleRepository;
    }

    @GetMapping
    public ResponseEntity<?> getUser() {
        return repository.findById(getUserId())
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getUserGroups() {
        Long userId = getUserId();

        List<Group> groups = groupUserRoleRepository.findByUserId(userId)
                .stream()
                .map(groupUser -> groupUser.group)
                .toList();

        List<GroupDTO> groupDTOs = groups.stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(groupDTOs);
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDTO dto) {
        Collection<String> missingFields = UserRegisterDTO.verify(dto);
        if (!missingFields.isEmpty()) return Responses.incompleteBody(missingFields);

        UserService userService = context.getBean(UserService.class);
        boolean isUnique = userService.isUserUnique(dto);

        if (!isUnique) {
            return Responses.error("Either the email or username is already in use.");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = new User();
        user.username = dto.username;
        user.firstName = dto.firstName;
        user.lastName = dto.lastName;
        user.password = passwordEncoder.encode(dto.password);
        user.email = dto.email;

        user.imageUrl = "pf_default.png"; // TODO: Set real image as default. Keep the name, used in the update
                                          //  method within an if check to not delete the default image

        User savedUser = repository.save(user);
        return Responses.ok("token", jwtUtils.generateToken(savedUser.id));
    }

    // TODO: Didn't delete the old image once, couldn't replicate it afterwards though.
    @PutMapping
    public ResponseEntity<?> update(@RequestPart(value = "file", required = false) MultipartFile file,
                                    @RequestPart(value = "dto", required = false) UserUpdateDTO dto) {
        Long userId = getUserId();
        Optional<User> user = repository.findById(userId);

        if (user.isEmpty()) return Responses.notFound("User with id " + userId + " not found");

        // Uploading a new file is not mandatory, only do something if a file has been sent.
        if(file != null) {
            ResponseEntity<?> fileUploadResult = fileUploadHandler.uploadFile(file, FileType.PROFILE_PICTURE, userId);
            if (fileUploadResult.getStatusCode() != HttpStatus.OK) return fileUploadResult;

            fileUploadHandler.handleOkResponse(fileUploadResult, user.get(), FileType.PROFILE_PICTURE);
        }

        // Only change data if a data has been sent, as it is not mandatory.
        if(dto != null) {
            if (dto.firstName != null) user.get().firstName = dto.firstName;
            if (dto.lastName != null) user.get().lastName = dto.lastName;
        }

        repository.save(user.get());
        return ResponseEntity.ok().body(new UserDTO(user.get()));
    }

    /**
     * Simple method to remove repetitive long line copies and pastes.
     *
     * @return the userId from the JWT token.
     */
    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
