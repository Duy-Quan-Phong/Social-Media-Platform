package com.codegym.socialmedia.model.Reel;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "reels")
public class Reel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private String thumbnailUrl;

    @Column(length = 500)
    private String caption;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private int viewsCount = 0;
    private int likesCount = 0;
    private int commentsCount = 0;
    private int sharesCount = 0;

    @Column(length = 1000)
    private String hashtags;

    private int durationSeconds;

    // ==========================================================
    // ⭐ PHẦN MỚI THÊM VÀO ĐỂ SỬA LỖI
    // ==========================================================
    @Transient // Đánh dấu để Hibernate KHÔNG tạo cột này trong Database
    private boolean isLiked;
    // Khi thêm biến này, Lombok @Data sẽ tự tạo hàm setIsLiked() và isLiked()
}