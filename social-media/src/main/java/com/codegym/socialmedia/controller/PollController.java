package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.PollDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.post.PollService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    @Autowired
    private PollService pollService;
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @RequestParam Long postId,
            @RequestParam String question,
            @RequestParam List<String> options,
            @RequestParam(required = false) String endsAt) {
        LocalDateTime end = endsAt != null && !endsAt.isBlank()
                ? LocalDateTime.parse(endsAt) : null;
        var poll = pollService.createPoll(postId, question, options, end);
        return ResponseEntity.ok(Map.of("success", true, "pollId", poll.getId()));
    }

    @PostMapping("/{pollId}/vote/{optionId}")
    public ResponseEntity<PollDto> vote(@PathVariable Long pollId, @PathVariable Long optionId) {
        User current = userService.getCurrentUser();
        return ResponseEntity.ok(pollService.vote(pollId, optionId, current));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<PollDto> getByPost(@PathVariable Long postId) {
        User current = userService.getCurrentUser();
        PollDto dto = pollService.getPollDto(postId, current);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }
}
