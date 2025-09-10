package com.codegym.socialmedia.model.conversation;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ConversationParticipantId implements Serializable {
    private Long conversationId;
    private Long userId;
}