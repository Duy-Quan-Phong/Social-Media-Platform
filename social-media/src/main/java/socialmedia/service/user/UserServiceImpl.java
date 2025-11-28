package socialmedia.service.user;

import socialmedia.component.CloudinaryService;
import socialmedia.dto.UserDTO;
import socialmedia.dto.UserRegistrationDto;
import socialmedia.model.account.NotificationSettings;
import socialmedia.model.account.Role;
import socialmedia.model.account.User;
import socialmedia.model.account.UserPrivacySettings;
import socialmedia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import socialmedia.repository.FriendshipRepository;
import socialmedia.repository.UserRepository;
import socialmedia.repository.RoleRepository;
import socialmedia.repository.UserPrivacySettingsRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static socialmedia.service.user.CustomOAuth2UserService.fromUrl;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository iUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserPrivacySettingsRepository userPrivacySettingsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private FriendshipRepository friendshipRepository; // Đã thêm @Autowired

    public User save(UserRegistrationDto registrationDto) {
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhone(registrationDto.getPhone());
        user.setDateOfBirth(registrationDto.getDateOfBirth());
        user.setLoginMethod(User.LoginMethod.EMAIL);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setActive(true);
        user.setVerified(false);
        user.setProfilePicture("https://res.cloudinary.com/dryyvmkwo/image/upload/v1748588721/samples/landscapes/nature-mountains.jpg");

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(new HashSet<>(Arrays.asList(roleUser)));
        UserPrivacySettings privacySettings = new UserPrivacySettings();
        privacySettings.setUser(user);

        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setUser(user);

        user.setPrivacySettings(privacySettings);
        user.setNotificationSettings(notificationSettings);

        return iUserRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return getUserByUsername(username);
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = (String) oauth2User.getAttribute("email");
            // Sửa lỗi: Lấy từ Optional ra
            return iUserRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    @Override
    public List<UserDTO> searchUsers(String keyword, Long currentUserId) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        List<User> friends = friendshipRepository.findFriendsByKeyword(currentUserId, keyword);
        return friends.stream()
                .map(u -> new UserDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getFirstName() + " " + u.getLastName(),
                        u.getProfilePicture()
                ))
                .toList();
    }

    @Override
    public User getUserById(Long id) {
        return iUserRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        // Sửa lỗi: Lấy từ Optional ra
        return iUserRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User save(User newUser) {
        User existingUser = getUserByUsername(newUser.getUsername());
        if (existingUser == null) {
            newUser.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));
        } else {
            if (!passwordEncoder.matches(newUser.getPasswordHash(), existingUser.getPasswordHash())) {
                newUser.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));
            } else {
                newUser.setPasswordHash(existingUser.getPasswordHash());
            }
        }
        return iUserRepository.save(newUser);
    }

    @Override
    public User save(User user, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            user.setProfilePicture(cloudinaryService.upload(image));
        }
        return iUserRepository.save(user);
    }

    @Override
    public void refreshAuthentication(String username) {
        UserDetails updatedUser = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        updatedUser,
                        updatedUser.getPassword(),
                        updatedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    // --- SỬA LỖI Ở ĐÂY ---
    public User findByEmail(String email) {
        return iUserRepository.findByEmail(email).orElse(null);
    }

    public boolean existsByUsername(String username) {
        return iUserRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return iUserRepository.existsByEmail(email);
    }

    // (Phần còn lại giữ nguyên logic OAuth2...)
    public User createOrUpdateOAuth2User(String email, String name, String provider, String avatar) {
        User user = findByEmail(email); // Giờ hàm này đã trả về User hoặc null, nên logic dưới đây chạy đúng

        if (user == null) {
            user = new User();
            user.setEmail(email);
            String baseUsername = email.split("@")[0];
            String username = generateUniqueUsername(baseUsername);
            user.setUsername(username);

            try {
                MultipartFile avatarFile = fromUrl(avatar, "avatar.jpg");
                avatar = cloudinaryService.upload(avatarFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            user.setProfilePicture(avatar);
            user.setPasswordHash("");

            if (name != null && !name.isEmpty()) {
                String[] nameParts = name.split(" ", 2);
                user.setFirstName(nameParts[0]);
                if (nameParts.length > 1) {
                    user.setLastName(nameParts[1]);
                }
            }

            if ("google".equalsIgnoreCase(provider)) {
                user.setLoginMethod(User.LoginMethod.GOOGLE);
            } else if ("facebook".equalsIgnoreCase(provider)) {
                user.setLoginMethod(User.LoginMethod.FACEBOOK);
            } else {
                user.setLoginMethod(User.LoginMethod.EMAIL);
            }

            user.setAccountStatus(User.AccountStatus.ACTIVE);
            user.setActive(true);
            user.setVerified(true);

            UserPrivacySettings privacySettings = new UserPrivacySettings();
            privacySettings.setUser(user);
            NotificationSettings notificationSettings = new NotificationSettings();
            notificationSettings.setUser(user);
            user.setPrivacySettings(privacySettings);
            user.setNotificationSettings(notificationSettings);

            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRoles(new HashSet<>(Arrays.asList(roleUser)));

            return iUserRepository.save(user);
        } else {
            if (name != null && !name.isEmpty()) {
                String[] nameParts = name.split(" ", 2);
                if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
                    user.setFirstName(nameParts[0]);
                }
                if (nameParts.length > 1 && (user.getLastName() == null || user.getLastName().isEmpty())) {
                    user.setLastName(nameParts[1]);
                }
            }
            if (user.getLoginMethod() == User.LoginMethod.EMAIL) {
                if ("google".equalsIgnoreCase(provider)) {
                    user.setLoginMethod(User.LoginMethod.GOOGLE);
                } else if ("facebook".equalsIgnoreCase(provider)) {
                    user.setLoginMethod(User.LoginMethod.FACEBOOK);
                }
            }
            return iUserRepository.save(user);
        }
    }

    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        username = username.replaceAll("[^a-zA-Z0-9_]", "");
        if (username.isEmpty()) username = "user";
        while (existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }

    public List<User> getAllUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return iUserRepository.findAllById(ids);
    }

    public long countUsers() {
        return iUserRepository.count();
    }

    public void deleteAllUsers() {
        iUserRepository.deleteAll();
    }
}