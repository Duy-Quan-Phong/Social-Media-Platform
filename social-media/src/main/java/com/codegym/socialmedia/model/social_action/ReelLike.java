package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.Reel.Reel;
import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "reel_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "reel_id"}) // Đảm bảo 1 người chỉ like 1 video 1 lần
})
public class ReelLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reel_id", nullable = false)
    private Reel reel;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người bấm like

    private LocalDateTime likedAt = LocalDateTime.now();

    public ReelLike(Reel reel, User user) {
        this.reel = reel;
        this.user = user;
    }
}