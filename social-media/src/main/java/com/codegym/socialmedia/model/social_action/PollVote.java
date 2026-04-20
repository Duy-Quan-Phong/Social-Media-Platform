package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "poll_votes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollVote {

    @EmbeddedId
    private PollVoteId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("optionId")
    @JoinColumn(name = "option_id")
    @ToString.Exclude
    private PollOption option;

    @CreationTimestamp
    private LocalDateTime votedAt;
}
