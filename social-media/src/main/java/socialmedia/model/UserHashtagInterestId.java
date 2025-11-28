package socialmedia.model;

import jakarta.persistence.Column; // Nhớ import cái này
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserHashtagInterestId implements Serializable {

    @Column(name = "user_id") // Định danh rõ tên cột
    private Long userId;

    @Column(name = "hashtag_id") // Định danh rõ tên cột
    private Long hashtagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserHashtagInterestId that = (UserHashtagInterestId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(hashtagId, that.hashtagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, hashtagId);
    }
}