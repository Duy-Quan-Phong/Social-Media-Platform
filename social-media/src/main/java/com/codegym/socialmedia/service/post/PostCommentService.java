package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.comment.DisplayCommentDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.PostComment;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PostCommentService {

    PostComment addComment(Long postId, User user, String content, List<Long> mentionIds);

    Page<DisplayCommentDTO> getCommentsByPost(Long postId, User currentUser, int page, int size);
    // Thêm method update comment
    PostComment updateComment(Long commentId, User currentUser, String newContent);

    PostComment deleteComment(Long commentId, User currentUser);
    // Thêm method lấy comment theo commentId
    Optional<PostComment> getCommentById(Long commentId);
    boolean toggleLikeComment(Long commentId, User currentUser);
    DisplayCommentDTO replyToComment(Long commentId, User currentUser, String content, List<Long> mentionIds);
}
