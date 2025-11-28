package socialmedia.controller;

import socialmedia.dto.friend.FriendDto;
import socialmedia.model.FriendRecommendation;
import socialmedia.model.account.User;
import socialmedia.repository.FriendRecommendationRepository;
import socialmedia.repository.UserRepository; // Import thêm cái này
import socialmedia.service.friend_ship.FriendshipService;
import socialmedia.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FriendController {
    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendRecommendationRepository friendRecRepo;

    @Autowired
    private UserRepository userRepository; // Inject User Repo để lấy list ngẫu nhiên

    @GetMapping("/friend/button")
    public String getFriendButtonFragment(Model model,
                                          @RequestParam String username,
                                          @RequestParam String friendshipStatus,
                                          @RequestParam boolean isSender,
                                          @RequestParam boolean isReceiver,
                                          @RequestParam boolean allowFriendRequests,
                                          @RequestParam boolean isVisible) {
        model.addAttribute("username", username);
        model.addAttribute("friendshipStatus", friendshipStatus);
        model.addAttribute("isSender", isSender);
        model.addAttribute("isReceiver", isReceiver);
        model.addAttribute("allowFriendRequests", allowFriendRequests);
        model.addAttribute("isVisible", isVisible);
        return "fragments/friend/friend-button-group :: buttonGroup";
    }

    @GetMapping("/friends")
    public String friends(Model model,
                          @RequestParam(value = "filter", defaultValue = "all") String filter,
                          @RequestParam(value = "targetUserId", required = false) Long targetUserId) {

        User currentUser = userService.getCurrentUser();

        // --- NẾU LÀ TAB GỢI Ý (NON-FRIENDS) ---
        if ("non-friends".equals(filter)) {

            // 1. Lấy AI Recommendations
            List<FriendRecommendation> aiRecs = friendRecRepo.findByUserIdOrderByScoreDesc(currentUser.getId());
            model.addAttribute("recommendations", aiRecs);

            // 2. Lấy Random Strangers (Phần này bạn đang thiếu)
            List<User> randomUsers = userRepository.findRandomStrangers(currentUser.getId());

            // Loại bỏ những người đã có trong AI Recs để tránh trùng lặp
            List<Long> aiIds = aiRecs.stream().map(r -> r.getSuggestedUser().getId()).collect(Collectors.toList());
            randomUsers.removeIf(u -> aiIds.contains(u.getId()));

            model.addAttribute("otherUsers", randomUsers); // <-- QUAN TRỌNG: Gửi sang View
        }

        model.addAttribute("isReceiver", false);
        model.addAttribute("isSender", false);
        model.addAttribute("targetUserId", targetUserId);
        model.addAttribute("filter", filter);
        // Đặt tiêu đề cho các tab khác (để JS dùng)
        String listTitle = "Danh sách bạn bè";
        if("sent-requests".equals(filter)) listTitle = "Lời mời đã gửi";
        else if("received-requests".equals(filter)) listTitle = "Lời mời kết bạn";
        model.addAttribute("listTitle", listTitle);

        return "friend/index";
    }

    // ... (Các API bên dưới giữ nguyên) ...
    @GetMapping("/api/friends")
    @ResponseBody
    public Page<FriendDto> getFriends(@RequestParam("page") int page,
                                      @RequestParam(value = "targetUserId", required = false) Long targetUserId,
                                      @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        User u = null;
        if(targetUserId == null) {
            u = userService.getCurrentUser();
        }else{
            u = userService.getUserById(targetUserId);
        }
        return friendshipService.getVisibleFriendList(u, page, size);
    }

    @GetMapping("/api/friends/mutual")
    @ResponseBody
    public Page<FriendDto> getMutualFriends(
            @RequestParam("targetUserId") Long targetUserId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        return friendshipService.findMutualFriends(currentUser.getId(), targetUserId, page, size);
    }

    @GetMapping("/api/friends/non-friends")
    @ResponseBody
    public Page<FriendDto> getNonFriends(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        return friendshipService.findNonFriends(currentUser.getId(), page, size);
    }

    @GetMapping("/api/friends/sent-requests")
    @ResponseBody
    public Page<FriendDto> getSentFriendRequests(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        return friendshipService.findSentFriendRequests(currentUser.getId(), page, size);
    }

    @GetMapping("/api/friends/received-requests")
    @ResponseBody
    public Page<FriendDto> getReceivedFriendRequests(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        return friendshipService.findReceivedFriendRequests(currentUser.getId(), page, size);
    }

    @PostMapping("/addFriend")
    @ResponseBody
    public ResponseEntity<String> addFriend(@RequestParam String user_name) {
        User friend = userService.getUserByUsername(user_name);
        friendshipService.addFriendship(friend);
        return ResponseEntity.ok("success");
    }

    @PutMapping("/acceptFriend")
    @ResponseBody
    public ResponseEntity<String> acceptFriend(@RequestParam String user_name) {
        User friend = userService.getUserByUsername(user_name);
        friendshipService.acceptFriendship(friend);
        return ResponseEntity.ok("accept success");
    }

    @DeleteMapping("/deleteFriend")
    @ResponseBody
    public ResponseEntity<String> deleteFriend(@RequestParam String user_name) {
        User friend = userService.getUserByUsername(user_name);
        friendshipService.deleteFriendship(friend);
        return ResponseEntity.ok("delete success");
    }

    // API mới: Lấy danh sách AI Gợi ý
    @GetMapping("/api/friends/recommendations")
    @ResponseBody
    public List<FriendDto> getAiRecommendations() {
        User currentUser = userService.getCurrentUser();
        List<FriendRecommendation> recs = friendRecRepo.findByUserIdOrderByScoreDesc(currentUser.getId());
        return recs.stream().map(rec -> {
            User u = rec.getSuggestedUser();
            FriendDto dto = new FriendDto(u, 0); // Dùng constructor có sẵn
            dto.setReason(rec.getReason());
            return dto;
        }).collect(Collectors.toList());
    }
    // Thêm API này vào FriendController.java
    @PostMapping("/api/friends/request/{userId}")
    @ResponseBody
    public ResponseEntity<?> sendFriendRequestById(@PathVariable Long userId) {
        User targetUser = userService.getUserById(userId);
        if (targetUser == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        friendshipService.addFriendship(targetUser);
        return ResponseEntity.ok("success");
    }
}