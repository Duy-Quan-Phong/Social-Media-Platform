package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.Reel.Reel;
import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reel_shares")
public class ReelShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reel_id", nullable = false)
    private Reel reel;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Có thể thêm trường để lưu trữ nơi chia sẻ (ví dụ: "Facebook", "Message", "Copy Link")
    private String sharePlatform;

    private LocalDateTime sharedAt = LocalDateTime.now();
}
