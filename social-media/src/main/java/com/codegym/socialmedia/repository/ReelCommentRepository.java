package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.social_action.ReelComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReelCommentRepository extends JpaRepository<ReelComment, Long> {

    // Tìm comment theo Reel ID, sắp xếp mới nhất lên đầu
    List<ReelComment> findByReelIdOrderByCommentedAtAsc(Long reelId);
}