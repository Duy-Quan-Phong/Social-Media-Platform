package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class SendMessageRequest {
    private Long conversationId;       // ưu tiên dùng
    private Long receiverId;           // fallback cho private chat cũ
    private String content;
    private List<AttachmentDto> attachments = new ArrayList<>();  // Initialized
}
