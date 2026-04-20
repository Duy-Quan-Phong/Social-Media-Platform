package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.post.PostCreateDto;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.dto.post.PostUpdateDto;
import com.codegym.socialmedia.model.PrivacyLevel;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.post.PollService;
import com.codegym.socialmedia.service.post.PostService;
import com.codegym.socialmedia.service.post.PostCommentService;
import com.codegym.socialmedia.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostCommentService commentService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private PollService pollService;

    @Autowired
    private ObjectMapper objectMapper;
    // LOẠI BỎ: @Autowired private PostRepository postRepository;
    // LOẠI BỎ: (tạm thời vì chưa implement)

    // ================== WEB PAGES ==================

    @GetMapping("/user/{username}")
    public String userPosts(@PathVariable String username,
                            Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size) {
        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "error/404";
        }

        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<PostDisplayDto> posts;
        if (currentUser != null) {
            posts = postService.getPostsByUser(targetUser, currentUser, pageable);
        } else {
            posts = postService.getPublicPostsByUser(targetUser, currentUser, pageable);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("isOwner", currentUser != null && currentUser.getId().equals(targetUser.getId()));

        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            model.addAttribute("postCreateDto", new PostCreateDto());
            model.addAttribute("privacyLevels", PrivacyLevel.values());
        }

        return "posts/user-posts";
    }

    // ================== CRUD OPERATIONS ==================

    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute PostCreateDto dto,
                             BindingResult result,
                             @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                             @RequestParam(value = "pollJson", required = false) String pollJson,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
            return "redirect:" + (redirectUrl != null ? redirectUrl : "/news-feed");
        }

        try {
            Post post = postService.createPost(dto, currentUser);
            if (pollJson != null && !pollJson.isBlank()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pollData = objectMapper.readValue(pollJson, Map.class);
                    String question = (String) pollData.get("question");
                    @SuppressWarnings("unchecked")
                    List<String> options = (List<String>) pollData.get("options");
                    int durationDays = pollData.get("durationDays") instanceof Number n ? n.intValue() : 7;
                    java.time.LocalDateTime endsAt = durationDays > 0
                            ? java.time.LocalDateTime.now().plusDays(durationDays) : null;
                    if (question != null && options != null && options.size() >= 2) {
                        pollService.createPoll(post.getId(), question, options, endsAt);
                    }
                } catch (Exception ignored) {}
            }
            redirectAttributes.addFlashAttribute("success", "Đăng bài thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:" + (redirectUrl != null ? redirectUrl : "/news-feed");
    }


    @PostMapping("/update/{id}")
    public String updatePost(@PathVariable Long id,
                             @Valid @ModelAttribute PostUpdateDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
            return "redirect:/posts";
        }

        try {
            dto.setId(id);
            postService.updatePost(id, dto, currentUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật bài viết thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/posts";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            postService.deletePost(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Xóa bài viết thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/posts";
    }

    // ================== AJAX API ENDPOINTS ==================
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<?> searchPosts(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "username") String username) {

        User user = userService.getUserByUsername(username);
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostDisplayDto> posts = postService.searchUserPosts(user, currentUser, keyword, pageable);

        return ResponseEntity.ok(posts);
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPostApi(@Valid @ModelAttribute PostCreateDto dto,
                                                             BindingResult result) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Post post = postService.createPost(dto, currentUser);
            PostDisplayDto postDto = postService.getPostById(post.getId(), currentUser);

            response.put("success", true);
            response.put("message", "Đăng bài thành công!");
            response.put("post", postDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/feed")
    @ResponseBody
    public ResponseEntity<?> getNewsFeed(
            @RequestParam(value = "postID", defaultValue = "-1") long postID,
            @RequestParam(value = "commentID", defaultValue = "-1") long commentID,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        if (commentID != -1) {
            PostComment comment = commentService.getCommentById(commentID).get();
            postID = comment.getPost().getId();
        }

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        if (postID == -1 && commentID == -1) {
            Pageable pageable = PageRequest.of(page, size);
            Page<PostDisplayDto> posts = postService.getPostsForNewsFeed(currentUser, pageable);

            return ResponseEntity.ok(posts);
        }

        PostDisplayDto postDto = postService.getPostById(postID, currentUser);
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/api/user/{username}")
    @ResponseBody
    public ResponseEntity<?> getUserPosts(
            @PathVariable String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = userService.getCurrentUser();


        Pageable pageable = PageRequest.of(page, size);

        Page<PostDisplayDto> posts;
        if (currentUser != null) {
            posts = postService.getPostsByUser(targetUser, currentUser, pageable);
        } else {
            posts = postService.getPublicPostsByUser(targetUser, currentUser, pageable);
        }

        return ResponseEntity.ok(posts);

    }

    @GetMapping("/api/user/{username}/photos")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getUserPhotos(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        int safeSize = Math.min(size, 50);
        User currentUser = userService.getCurrentUser();

        try {
            // Get posts with images
            Page<PostDisplayDto> posts;
            if (currentUser != null) {
                posts = postService.getPostsByUser(targetUser, currentUser, PageRequest.of(page, safeSize));
            } else {
                posts = postService.getPublicPostsByUser(targetUser, currentUser, PageRequest.of(page, safeSize));
            }

            // Extract all images from posts
            List<Map<String, String>> photos = new ArrayList<>();
            posts.getContent().forEach(post -> {
                if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
                    post.getImageUrls().forEach(imageUrl -> {
                        Map<String, String> photo = new HashMap<>();
                        photo.put("url", imageUrl);
                        photo.put("postId", post.getId().toString());
                        photos.add(photo);
                    });
                }
            });

            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/api/like/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean isLiked = postService.toggleLike(id, currentUser);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("message", isLiked ? "Đã thích" : "Đã bỏ thích");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/api/react/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleReaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        String reactionType = body.get("reactionType");
        if (reactionType == null || reactionType.isBlank()) {
            response.put("success", false);
            response.put("message", "Loại cảm xúc không hợp lệ");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String newReaction = postService.toggleReaction(id, currentUser, reactionType);
            response.put("success", true);
            response.put("newReaction", newReaction);
            response.put("isLiked", newReaction != null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<PostDisplayDto> getPost(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();

        try {
            PostDisplayDto post = postService.getPostById(id, currentUser);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePostApi(@PathVariable Long id,
                                                             @Valid @ModelAttribute PostUpdateDto dto,
                                                             BindingResult result) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            dto.setId(id);
            Post post = postService.updatePost(id, dto, currentUser);
            PostDisplayDto postDto = postService.getPostById(post.getId(), currentUser);

            response.put("success", true);
            response.put("message", "Cập nhật bài viết thành công!");
            response.put("post", postDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePostApi(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            postService.deletePost(id, currentUser);
            response.put("success", true);
            response.put("message", "Xóa bài viết thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/api/save/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSave(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean saved = postService.toggleSavePost(id, currentUser);
            response.put("success", true);
            response.put("saved", saved);
            response.put("message", saved ? "Đã lưu bài viết" : "Đã bỏ lưu bài viết");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/saved")
    @ResponseBody
    public ResponseEntity<?> getSavedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getSavedPosts(currentUser, pageable));
    }

    @GetMapping("/saved")
    public String savedPostsPage(org.springframework.ui.Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return "redirect:/login";
        model.addAttribute("currentUser", currentUser);
        return "saved-posts";
    }

    // ── Share post ────────────────────────────────────────────────────────────

    @PostMapping("/api/share/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sharePost(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();

        try {
            String comment = body != null ? body.getOrDefault("comment", "") : "";
            Post shared = postService.sharePost(id, currentUser, comment);
            PostDisplayDto dto = postService.getPostById(shared.getId(), currentUser);
            return ResponseEntity.ok(Map.of("success", true, "post", dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── Friend suggestions endpoint ───────────────────────────────────────────

    @GetMapping("/api/suggestions/friends")
    @ResponseBody
    public ResponseEntity<?> friendSuggestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(friendshipService.getFriendSuggestions(
                currentUser.getId(), page, Math.min(size, 10)));
    }

    // ── Hashtag endpoints ─────────────────────────────────────────────────────

    @GetMapping("/api/search/global")
    @ResponseBody
    public ResponseEntity<?> globalSearch(
            @RequestParam("q") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, Math.min(size, 20));
        if (q.startsWith("#")) {
            String tag = q.substring(1);
            return ResponseEntity.ok(postService.getPostsByHashtag(tag, currentUser, pageable));
        }
        return ResponseEntity.ok(postService.searchPublicPosts(q, currentUser, pageable));
    }

    @GetMapping("/api/trending-hashtags")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> trendingHashtags(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getTrendingHashtags(Math.min(limit, 20)));
    }
}