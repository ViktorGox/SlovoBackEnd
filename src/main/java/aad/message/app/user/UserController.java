package aad.message.app.user;

import aad.message.app.acess.token.AccessTokenService;
import aad.message.app.filetransfer.FileType;
import aad.message.app.filetransfer.FileUploadHandler;
import aad.message.app.group.Group;
import aad.message.app.group.GroupDTO;
import aad.message.app.group.user.role.GroupUserRoleRepository;
import aad.message.app.jwt.JwtUtils;
import aad.message.app.refresh.token.RefreshTokenService;
import aad.message.app.returns.Responses;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;

    public UserController(UserRepository repository,
                          ApplicationContext context,
                          FileUploadHandler fileUploadHandler,
                          JwtUtils jwtUtils,
                          GroupUserRoleRepository groupUserRoleRepository, UserRepository userRepository, RefreshTokenService refreshTokenService, AccessTokenService accessTokenService) {
        this.repository = repository;
        this.context = context;
        this.fileUploadHandler = fileUploadHandler;
        this.jwtUtils = jwtUtils;
        this.groupUserRoleRepository = groupUserRoleRepository;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenService = accessTokenService;
    }

    @GetMapping
    public ResponseEntity<?> getUser() {
        return repository.findById(getUserId())
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUser(@RequestParam String query) {
        Optional<User> user = repository.findByUsername(query)
                .or(() -> repository.findByEmail(query));

        return user.map(value -> ResponseEntity.ok(new UserDTO(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
    public ResponseEntity<?> register(@RequestPart(value = "dto") UserRegisterDTO dto,
                                      @RequestPart(value = "file", required = false) MultipartFile file) {
        UserService userService = context.getBean(UserService.class);
        String uniquenessError = userService.checkUserUniqueness(dto);

        if (uniquenessError != null) {
            return Responses.error(uniquenessError);
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = new User();
        user.username = dto.username;
        user.firstName = dto.firstName;
        user.lastName = dto.lastName;
        user.password = passwordEncoder.encode(dto.password);
        user.email = dto.email;

        user.imageUrl = "pf_default.png";

        User savedUser = repository.save(user);

        if (file != null && !file.isEmpty()) {
            ResponseEntity<?> fileUploadResult = fileUploadHandler.uploadFile(file, FileType.PROFILE_PICTURE, savedUser.id);
            if (fileUploadResult.getStatusCode() != HttpStatus.OK) return fileUploadResult;


            savedUser.imageUrl = fileUploadHandler.okFileName(fileUploadResult);

            userRepository.save(savedUser);
        }

        String accessToken = jwtUtils.generateAccessToken(savedUser);
        String refreshToken = jwtUtils.generateRefreshToken(savedUser);

        return ResponseEntity.ok()
                .body(Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                ));
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestPart(value = "file", required = false) MultipartFile file,
                                    @RequestPart(value = "dto", required = false) UserUpdateDTO dto) {
        Long userId = getUserId();
        Optional<User> userOptional = repository.findById(userId);

        if (userOptional.isEmpty()) return Responses.notFound("User with id " + userId + " not found");

        User user = userOptional.get();

        // Uploading a new file is not mandatory, only do something if a file has been sent.
        if(file != null) {
            ResponseEntity<?> fileUploadResult = fileUploadHandler.uploadFile(file, FileType.PROFILE_PICTURE, userId);
            if (fileUploadResult.getStatusCode() != HttpStatus.OK) return fileUploadResult;

            fileUploadHandler.handleOkResponse(fileUploadResult, user, FileType.PROFILE_PICTURE);
        }

        // Only change data if a data has been sent, as it is not mandatory.
        if(dto != null) {
            if (dto.firstName != null) user.firstName = dto.firstName;
            if (dto.lastName != null) user.lastName = dto.lastName;
            if (dto.email != null) user.email = dto.email;
        }

        repository.save(user);
        return ResponseEntity.ok().body(new UserDTO(userOptional.get()));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout() {
        Long userId = getUserId();

        refreshTokenService.deleteByUserId(userId);
        accessTokenService.deleteByUserId(userId);

        return Responses.ok("msg", "Logout successful");
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
