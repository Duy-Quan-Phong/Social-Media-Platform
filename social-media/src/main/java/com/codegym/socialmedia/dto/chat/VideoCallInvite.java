package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoCallInvite {
    private Long callerId;
    private Long conversationId;
    private String callerName;
    private String callerAvatarUrl; // có thể null nếu chưa có avatar
}
