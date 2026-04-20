package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.block.BlockService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/block")
public class BlockController {

    @Autowired
    private BlockService blockService;

    @Autowired
    private UserService userService;

    @PostMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> blockUser(@PathVariable Long userId) {
        User me = userService.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body(Map.of("success", false));
        User target = userService.getUserById(userId);
        if (target == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Người dùng không tồn tại"));
        blockService.blockUser(me, target);
        return ResponseEntity.ok(Map.of("success", true, "blocked", true, "message", "Đã chặn người dùng"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> unblockUser(@PathVariable Long userId) {
        User me = userService.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body(Map.of("success", false));
        User target = userService.getUserById(userId);
        if (target == null) return ResponseEntity.badRequest().body(Map.of("success", false));
        blockService.unblockUser(me, target);
        return ResponseEntity.ok(Map.of("success", true, "blocked", false, "message", "Đã bỏ chặn người dùng"));
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> getBlockStatus(@PathVariable Long userId) {
        User me = userService.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body(Map.of("success", false));
        boolean blocked = blockService.isBlocked(me.getId(), userId);
        return ResponseEntity.ok(Map.of("success", true, "blocked", blocked));
    }
}
