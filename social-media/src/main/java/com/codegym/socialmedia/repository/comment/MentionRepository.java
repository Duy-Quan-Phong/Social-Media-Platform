package com.codegym.socialmedia.repository.comment;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentionRepository extends JpaRepository<CommentMention,Long> {
    @Query("SELECT cm.mentionedUser FROM CommentMention cm WHERE cm.comment.id = :commentId")
    List<User> getUsersByCommentId(@Param("commentId") Long commentId);
}
