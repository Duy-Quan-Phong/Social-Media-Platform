package com.codegym.socailmedia.repository.comment;

import com.codegym.socailmedia.model.social_action.LikeComment;
import com.codegym.socailmedia.model.social_action.LikeCommentId;
import com.codegym.socailmedia.model.social_action.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<LikeComment, LikeCommentId> {

    Optional<LikeComment> findById(LikeCommentId id);

    int countByComment(PostComment comment);

}
