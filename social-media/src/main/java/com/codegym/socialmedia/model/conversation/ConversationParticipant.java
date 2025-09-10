package com.codegym.socialmedia.model.conversation;

import com.codegym.socialmedia.model.account.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationParticipant {

    @EmbeddedId
    private ConversationParticipantId id;

    private Long lastReadMessageId;

    @ManyToOne
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean isMuted = false;
    private Boolean isActive;

    private String nickname;

    public enum Role {
        ADMIN, MEMBER
    }
}
