package com.codegym.socialmedia.model.conversation;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    private String content;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAttachment> attachments = new ArrayList<>();

    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    private Boolean isDeleted;
    private Boolean isRecalled;

    private Integer callDuration;

    @Enumerated(EnumType.STRING)
    private CallStatus callStatus;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<MessageRead> messageReads;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;  // Added to Message entity for consistency

    public enum CallStatus {
        PENDING, MISSED, COMPLETED, FAILED
    }

    public enum MessageType {  // Moved to Message for better organization, but can keep in Attachment if preferred
        TEXT, IMAGE, VIDEO, FILE, AUDIO, CALL
    }

    @Override
    public String toString() {
        return content;
    }
}
