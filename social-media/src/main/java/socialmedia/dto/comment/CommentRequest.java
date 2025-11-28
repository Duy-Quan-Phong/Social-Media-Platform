package socialmedia.dto.comment;

import lombok.Data;

import java.util.List;

@Data
public class CommentRequest {
    private Long postId;
    private String content;
    private List<Long> mentionedUserIds;
}