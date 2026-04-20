package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.repository.post.PostRepository;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    @Autowired
    private UserService userService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("query", q != null ? q : "");
        return "search";
    }

    @GetMapping("/api/search/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String q) {
        if (q == null || q.trim().length() < 1) return ResponseEntity.ok(List.of());
        User me = userService.getCurrentUser();
        Long myId = me != null ? me.getId() : -1L;
        var users = userRepository.searchUsersExcludeCurrent(q.trim(), myId, PageRequest.of(0, 10));
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("fullName", u.getFirstName() + " " + u.getLastName());
            map.put("avatarUrl", u.getProfilePicture());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/search/posts")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchPosts(@RequestParam String q) {
        if (q == null || q.trim().length() < 1) return ResponseEntity.ok(List.of());
        User me = userService.getCurrentUser();
        Long myId = me != null ? me.getId() : -1L;
        var posts = postRepository.searchVisiblePosts(q.trim(), myId, PageRequest.of(0, 10));
        List<Map<String, Object>> result = posts.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("content", p.getContent() != null && p.getContent().length() > 150
                ? p.getContent().substring(0, 150) + "..." : p.getContent());
            map.put("authorName", p.getUser().getFirstName() + " " + p.getUser().getLastName());
            map.put("authorUsername", p.getUser().getUsername());
            map.put("authorAvatar", p.getUser().getProfilePicture());
            map.put("createdAt", p.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
