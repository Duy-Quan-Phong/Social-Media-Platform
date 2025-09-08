package com.codegym.socialmedia.repository.comment;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.LikeComment;
import com.codegym.socialmedia.model.social_action.LikeCommentId;
import com.codegym.socialmedia.model.social_action.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface LikeCommentRepository extends JpaRepository<LikeComment, LikeCommentId> {
    boolean existsByUserAndComment(User user, PostComment comment);
    int countByComment(PostComment comment);
    void deleteByUserAndComment(User user, PostComment comment);
    List<LikeComment> findAllByComment(PostComment comment);

}


