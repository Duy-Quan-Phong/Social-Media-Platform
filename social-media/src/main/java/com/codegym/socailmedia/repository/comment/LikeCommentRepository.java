package com.codegym.socailmedia.repository.comment;

import com.codegym.socailmedia.model.social_action.LikeComment;
import com.codegym.socailmedia.model.social_action.LikeCommentId;
import com.codegym.socailmedia.model.social_action.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeCommentRepository extends JpaRepository<LikeComment, LikeCommentId> {

    int countByComment(PostComment comment);

    @Modifying
    @Query("delete from LikeComment l where l.comment.id = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);
}


