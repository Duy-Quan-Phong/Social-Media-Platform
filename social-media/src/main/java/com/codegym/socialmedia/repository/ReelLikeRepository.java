package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.social_action.ReelLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReelLikeRepository extends JpaRepository<ReelLike, Long> {
    // Kiểm tra xem User A đã like Reel B chưa
    boolean existsByReelIdAndUserId(Long reelId, Long userId);

    // Tìm để xóa (khi Unlike)
    ReelLike findByReelIdAndUserId(Long reelId, Long userId);
}