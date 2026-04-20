package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.post.PostService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class HashtagController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;

    @GetMapping("/hashtag/{tag}")
    public String browseHashtag(@PathVariable String tag,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        User current = userService.getCurrentUser();
        Page<PostDisplayDto> posts = postService.getPostsByHashtag(tag, current,
                PageRequest.of(page, Math.min(size, 30)));
        model.addAttribute("tag", tag);
        model.addAttribute("posts", posts);
        model.addAttribute("currentUser", current);
        return "hashtag/browse";
    }

    @GetMapping("/api/hashtags/trending")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> trending(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getTrendingHashtags(Math.min(limit, 20)));
    }
}
