package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectPayload {
    private Long conversationId;
    private Long rejecterId;
    private Long callMessageId;
    private Integer duration; // seconds, 0 if call was never answered
}
