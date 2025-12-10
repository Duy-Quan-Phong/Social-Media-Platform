package com.codegym.socailmedia.model;

import jakarta.persistence.*;
import lombok.Data;
import com.codegym.socailmedia.model.account.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "friend_recommendations") // Tên bảng trong MySQL
@Data
public class FriendRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId; // Gợi ý cho ai

    @ManyToOne
    @JoinColumn(name = "suggested_user_id") // Link tới bảng Users
    private User suggestedUser; // Người được gợi ý

    private Double score; // Điểm số
    private String reason; // Lý do (VD: "Có 5 bạn chung")

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}