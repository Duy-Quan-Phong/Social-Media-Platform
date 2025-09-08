package com.codegym.socialmedia.model.social_action;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeCommentId implements Serializable {
    private Long userId;
    private Long commentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikeCommentId)) return false;
        LikeCommentId that = (LikeCommentId) o;
        return userId != null && commentId != null &&
                userId.equals(that.userId) &&
                commentId.equals(that.commentId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, commentId);
    }

}