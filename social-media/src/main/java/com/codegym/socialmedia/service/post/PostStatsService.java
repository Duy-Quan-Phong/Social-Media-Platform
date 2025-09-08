package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.repository.post.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostStatsService {
    @Autowired
    private PostRepository postRepository;

    public long countUserPosts(User user) {
        // Sửa từ countByUser thành countByUserAndIsDeletedFalse
        return postRepository.countByUserAndIsDeletedFalse(user);
    }
}