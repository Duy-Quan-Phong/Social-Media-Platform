package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.StoryDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.post.StoryService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;
    @Autowired
    private UserService userService;

    @GetMapping("/feed")
    public ResponseEntity<List<StoryDto>> feed() {
        User current = userService.getCurrentUser();
        return ResponseEntity.ok(storyService.getStoriesFeed(current));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestParam("media") MultipartFile media,
            @RequestParam(value = "caption", defaultValue = "") String caption) {
        User current = userService.getCurrentUser();
        var story = storyService.createStory(current, media, caption);
        return ResponseEntity.ok(Map.of("success", true, "id", story.getId()));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> view(@PathVariable Long id) {
        storyService.viewStory(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        User current = userService.getCurrentUser();
        boolean ok = storyService.deleteStory(id, current);
        return ResponseEntity.ok(Map.of("success", ok));
    }
}
