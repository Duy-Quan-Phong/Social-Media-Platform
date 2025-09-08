package com.codegym.socialmedia.dto.comment;

import lombok.Data;

@Data
public class CommentRequest {
    private Long postId;
    private String content;
}