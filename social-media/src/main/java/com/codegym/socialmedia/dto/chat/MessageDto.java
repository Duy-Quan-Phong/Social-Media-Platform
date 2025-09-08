package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class MessageDto {
    private Long id;

    private Long conversationId;
    private String conversationType; // "private" | "group"

    private Long senderId;
    private String senderName;
    private String senderAvatar;

    private Long receiverId; // optional (tương thích cũ)
    private String content;

    // Thêm cho file
    private List<AttachmentDto> attachments = new ArrayList<>();  // Initialized to avoid null

    private LocalDateTime timestamp;
    private boolean isRead;

    private String type;  // Added for JS compatibility (e.g., primary type: TEXT, IMAGE, etc.)
}

