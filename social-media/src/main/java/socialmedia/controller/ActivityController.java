package socialmedia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import socialmedia.model.Hashtag;
import socialmedia.model.UserHashtagInterest;
import socialmedia.model.account.User;
import socialmedia.repository.UserRepository;
import socialmedia.repository.Hashtag.PostHashtagRepository;
import socialmedia.repository.Hashtag.UserHashtagInterestRepository;
import socialmedia.service.user.UserActivityService;
import socialmedia.service.user.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    // --- SERVICE CŨ CHO ONLINE STATUS ---
    @Autowired private UserActivityService userActivityService;
    @Autowired private UserService userService;

    // --- REPOSITORY MỚI CHO AI (THÊM VÀO) ---
    @Autowired private UserRepository userRepository;
    @Autowired private PostHashtagRepository postHashtagRepo;
    @Autowired private UserHashtagInterestRepository interestRepo;

    // 1. API kiểm tra trạng thái online (Giữ nguyên)
    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        boolean isOnline = userActivityService.isOnline(userId);
        String lastActivity = userActivityService.getLastActivityStatus(userId);
        response.put("online", isOnline);
        response.put("lastActivity", lastActivity);
        return ResponseEntity.ok(response);
    }

    // 2. API lấy danh sách online (Giữ nguyên)
    @GetMapping("/online-users")
    public ResponseEntity<Map<Long, String>> getOnlineUsers() {
        return ResponseEntity.ok(userActivityService.getOnlineUsers());
    }

    // 3. API Ping Activity (Giữ nguyên logic của bạn)
    @PostMapping("/ping")
    public ResponseEntity<String> pingActivity() {
        var currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            userActivityService.updateActivity(currentUser.getId());
            return ResponseEntity.ok("Activity updated");
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    // 4. [QUAN TRỌNG] API Hứng dữ liệu Click cho AI (BẠN ĐANG THIẾU CÁI NÀY)
    @PostMapping("/log")
    public ResponseEntity<?> logActivity(@RequestBody Map<String, String> payload,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.ok().build();

        try {
            String actionType = payload.get("actionType");
            Long postId = Long.parseLong(payload.get("postId"));

            // Lấy ID người dùng hiện tại

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user == null) {
                throw new RuntimeException("User not found");
            }

            if ("CLICK".equals(actionType)) {
                // Click xem chi tiết -> Cộng 1 điểm sở thích
                System.out.println("User " + user.getId() + " clicked post " + postId);
                updateInterests(user.getId(), postId, 1.0);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tracking AI: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // Hàm phụ trợ cộng điểm (Copy từ hướng dẫn trước)
    private void updateInterests(Long userId, Long postId, double scoreDelta) {
        try {
            List<Hashtag> hashtags = postHashtagRepo.findHashtagsByPostId(postId);
            for (Hashtag tag : hashtags) {
                UserHashtagInterest interest = interestRepo.findByUserIdAndHashtagId(userId, tag.getId())
                        .orElse(new UserHashtagInterest(userId, tag.getId(), 0.0));

                double newScore = interest.getInterestScore() + scoreDelta;
                interest.setInterestScore(newScore);
                interest.setLastInteraction(LocalDateTime.now());
                interestRepo.save(interest);
            }
        } catch (Exception e) {
            // Ignore error
        }
    }
}