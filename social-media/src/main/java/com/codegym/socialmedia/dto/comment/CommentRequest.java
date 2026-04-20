package com.codegym.socialmedia.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CommentRequest {
    private Long postId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(max = 1000, message = "Bình luận không được vượt quá 1000 ký tự")
    private String content;

    private List<Long> mentionedUserIds;
}
