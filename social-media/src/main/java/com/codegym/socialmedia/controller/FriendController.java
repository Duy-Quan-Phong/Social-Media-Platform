package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.friend.FriendDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FriendController {
    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserService userService;

    @GetMapping("/friend/button")
    public String getFriendButtonFragment(Model model,
                                          @RequestParam String username,
                                          @RequestParam String friendshipStatus,
                                          @RequestParam boolean isSender,
                                          @RequestParam boolean isReceiver,
                                          @RequestParam boolean allowFriendRequests,
                                          @RequestParam boolean isVisible,
                                          HttpServletRequest request) {
        model.addAttribute("username", username);
        model.addAttribute("friendshipStatus", friendshipStatus);
        model.addAttribute("isSender", isSender);
        model.addAttribute("isReceiver", isReceiver);
        model.addAttribute("allowFriendRequests", allowFriendRequests);
        model.addAttribute("isVisible", isVisible);
        return "fragments/friend/friend-button-group :: buttonGroup";
    }


    @GetMapping("/friends")
    public String friends(Model model, @RequestParam(value = "filter", defaultValue = "all") String filter
            ,@RequestParam(value = "targetUserId", required = false) Long targetUserId) {

        boolean isSender = false, isReceiver = false;
        model.addAttribute("isReceiver", isReceiver);
        model.addAttribute("isSender", isSender);
        model.addAttribute("targetUserId", targetUserId);
        model.addAttribute("filter", filter);
        return "friend/index";
    }

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
        Long currentUserId = currentUser.getId();
        return friendshipService.findMutualFriends(currentUserId, targetUserId, page, size);
    }

    @GetMapping("/api/friends/non-friends")
    @ResponseBody
    public Page<FriendDto> getNonFriends(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        Page<FriendDto> friendDtos = friendshipService.findNonFriends(currentUserId, page, size);
        return friendshipService.findNonFriends(currentUserId, page, size);
    }

    @GetMapping("/api/friends/sent-requests")
    @ResponseBody
    public Page<FriendDto> getSentFriendRequests(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        return friendshipService.findSentFriendRequests(currentUserId, page, size);
    }

    @GetMapping("/api/friends/received-requests")
    @ResponseBody
    public Page<FriendDto> getReceivedFriendRequests(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        return friendshipService.findReceivedFriendRequests(currentUserId, page, size);
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

}