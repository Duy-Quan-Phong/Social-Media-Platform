package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.service.user.BlockService;
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
    public ResponseEntity<Map<String, Object>> block(@PathVariable Long userId) {
        boolean ok = blockService.block(userId);
        return ResponseEntity.ok(Map.of("success", ok,
                "message", ok ? "Đã chặn người dùng" : "Không thể chặn"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> unblock(@PathVariable Long userId) {
        boolean ok = blockService.unblock(userId);
        return ResponseEntity.ok(Map.of("success", ok,
                "message", ok ? "Đã bỏ chặn" : "Không thể bỏ chặn"));
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Boolean>> status(@PathVariable Long userId) {
        Long currentId = userService.getCurrentUser().getId();
        return ResponseEntity.ok(Map.of(
                "blockedByMe", blockService.isBlocked(currentId, userId),
                "blockedByThem", blockService.isBlocked(userId, currentId)
        ));
    }
}
