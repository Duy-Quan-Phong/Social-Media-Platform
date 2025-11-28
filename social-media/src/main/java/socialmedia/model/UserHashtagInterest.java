package socialmedia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_hashtag_interests")
@Data
@NoArgsConstructor
public class UserHashtagInterest {

    @EmbeddedId
    private UserHashtagInterestId id;

    // --- ĐÃ XÓA 2 DÒNG userId VÀ hashtagId Ở ĐÂY ĐỂ TRÁNH LỖI DUPLICATE ---
    // Hibernate sẽ tự lấy ID từ biến 'id' ở trên.

    @Column(name = "interest_score")
    private Double interestScore;

    @Column(name = "last_interaction")
    private LocalDateTime lastInteraction;

    // Constructor giữ nguyên logic, chỉ cần gán vào 'id'
    public UserHashtagInterest(Long userId, Long hashtagId, Double interestScore) {
        this.id = new UserHashtagInterestId(userId, hashtagId);
        this.interestScore = interestScore;
        this.lastInteraction = LocalDateTime.now();
    }

    // Getter tiện ích (Optional) - Để code cũ gọi .getUserId() không bị lỗi
    public Long getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public Long getHashtagId() {
        return id != null ? id.getHashtagId() : null;
    }
}