package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.repository.post.PostRepository;
import com.codegym.socialmedia.service.post.PostService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private UserService userService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false, defaultValue = "") String q, Model model) {
        model.addAttribute("q", q);
        return "search/results";
    }

    @GetMapping("/api/search/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (q == null || q.trim().isEmpty()) return ResponseEntity.ok(List.of());

        User currentUser = userService.getCurrentUser();
        List<User> users;
        if (currentUser != null) {
            users = userRepository.searchUsersExcludeCurrent(q.trim(), currentUser.getId(),
                    PageRequest.of(page, size));
        } else {
            users = userRepository.searchUsers(q.trim(), PageRequest.of(page, size));
        }

        List<Map<String, Object>> result = users.stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "username", u.getUsername(),
                "fullName", u.getFirstName() + " " + u.getLastName(),
                "avatarUrl", u.getProfilePicture() != null ? u.getProfilePicture() : ""
        )).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/search/posts")
    @ResponseBody
    public ResponseEntity<Page<PostDisplayDto>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (q == null || q.trim().isEmpty()) return ResponseEntity.ok(Page.empty());

        User currentUser = userService.getCurrentUser();
        Page<PostDisplayDto> posts = postService.searchPublicPosts(q.trim(), currentUser,
                PageRequest.of(page, size));
        return ResponseEntity.ok(posts);
    }
}
