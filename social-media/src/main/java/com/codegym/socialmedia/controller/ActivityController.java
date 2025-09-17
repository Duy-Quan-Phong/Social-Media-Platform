package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.service.user.UserActivityService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserService userService;

    // API để kiểm tra trạng thái online của một user
    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        boolean isOnline = userActivityService.isOnline(userId);
        String lastActivity = userActivityService.getLastActivityStatus(userId);

        response.put("online", isOnline);
        response.put("lastActivity", lastActivity);

        return ResponseEntity.ok(response);
    }

    // API để lấy danh sách user đang online
    @GetMapping("/online-users")
    public ResponseEntity<Map<Long, String>> getOnlineUsers() {
        Map<Long, String> onlineUsers = userActivityService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

    // API để update activity (có thể gọi từ frontend khi có tương tác)
    @PostMapping("/ping")
    public ResponseEntity<String> pingActivity() {
        var currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            userActivityService.updateActivity(currentUser.getId());
            return ResponseEntity.ok("Activity updated");
        }
        return ResponseEntity.badRequest().body("User not found");
    }
}