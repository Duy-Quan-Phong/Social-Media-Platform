package socialmedia.repository.Hashtag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import socialmedia.model.UserHashtagInterest;
import socialmedia.model.UserHashtagInterestId; // Import thêm cái ID này

import java.util.Optional;

@Repository
public interface UserHashtagInterestRepository extends JpaRepository<UserHashtagInterest, UserHashtagInterestId> {

    // --- ĐOẠN SỬA QUAN TRỌNG ---
    // Vì userId nằm trong khóa chính phức hợp (EmbeddedId), ta phải dùng u.id.userId
    @Query("SELECT u FROM UserHashtagInterest u WHERE u.id.userId = :userId AND u.id.hashtagId = :hashtagId")
    Optional<UserHashtagInterest> findByUserIdAndHashtagId(@Param("userId") Long userId, @Param("hashtagId") Long hashtagId);
}