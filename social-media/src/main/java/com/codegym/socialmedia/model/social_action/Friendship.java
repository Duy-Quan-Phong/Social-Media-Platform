package com.codegym.socialmedia.model.social_action;
import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "friendships")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    @EmbeddedId
    private FriendshipId id;

    @ManyToOne
    @MapsId("requesterId") // ánh xạ requesterId trong FriendshipId
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @MapsId("addresseeId") // ánh xạ addresseeId trong FriendshipId
    @JoinColumn(name = "addressee_id")
    private User addressee;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    @CreationTimestamp
    private LocalDateTime requestedAt;

    public enum FriendshipStatus {
        PENDING, ACCEPTED, NONE
    }


}
