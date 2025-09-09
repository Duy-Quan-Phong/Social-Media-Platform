package com.codegym.socialmedia.repository.comment;

import com.codegym.socialmedia.model.social_action.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {
    // Tìm tất cả mentions trong một comment
    List<CommentMention> findByCommentId(Long commentId);

    // Tìm tất cả comment mà 1 user được tag
    List<CommentMention> findByMentionedUserId(Long userId);
}