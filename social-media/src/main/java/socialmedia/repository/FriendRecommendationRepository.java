package socialmedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import socialmedia.model.FriendRecommendation;
import java.util.List;

@Repository
public interface FriendRecommendationRepository extends JpaRepository<FriendRecommendation, Long> {

    // Hàm lấy danh sách gợi ý cho 1 user, sắp xếp điểm từ cao xuống thấp
    List<FriendRecommendation> findByUserIdOrderByScoreDesc(Long userId);
}