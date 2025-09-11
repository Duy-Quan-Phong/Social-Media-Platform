package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadNotification {
    private Long conversationId;
    private long unreadCount;
    private long totalUnread;
}
