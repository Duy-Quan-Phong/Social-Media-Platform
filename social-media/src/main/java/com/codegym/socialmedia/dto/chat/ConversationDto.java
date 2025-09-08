package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class ConversationDto {
    private String id;
    private String name;
    private String avatar;

    private String type; // "private" | "group"
    private Integer participantCount;
    private List<ParticipantDto> participants;

    private String lastMessage;
    private String timeAgo;
    private boolean isOnline;
    private boolean hasUnread;
    private int unreadCount;
    private LocalDateTime lastMessageTime;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ParticipantDto {
        private Long id;
        private String username;
        private String fullName;
        private String avatar;
        private String role;   // ADMIN | MEMBER
        private boolean online;
    }
}
