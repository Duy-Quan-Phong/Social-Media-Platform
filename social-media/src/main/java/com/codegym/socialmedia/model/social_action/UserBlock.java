package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock {

    @EmbeddedId
    private UserBlockId id;

    @ManyToOne
    @MapsId("blockerId")
    @JoinColumn(name = "blocker_id")
    private User blocker;

    @ManyToOne
    @MapsId("blockedId")
    @JoinColumn(name = "blocked_id")
    private User blocked;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
