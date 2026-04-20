package com.codegym.socialmedia.service.user;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.repository.post.PostLikeRepository;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.post.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserStatsService {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Cacheable(value = "userStats", key = "#user.id")
    public Map<String, Long> getUserStats(User user) {
        Map<String, Long> stats = new HashMap<>();

        try {
            stats.put("friends", (long) friendshipService.countFriends(user.getId()));
        } catch (Exception e) {
            stats.put("friends", 0L);
        }

        try {
            stats.put("posts", postService.countUserPosts(user));
        } catch (Exception e) {
            stats.put("posts", 0L);
        }

        try {
            stats.put("likes", postLikeRepository.countLikesReceivedByUser(user.getId()));
        } catch (Exception e) {
            stats.put("likes", 0L);
        }

        return stats;
    }
}