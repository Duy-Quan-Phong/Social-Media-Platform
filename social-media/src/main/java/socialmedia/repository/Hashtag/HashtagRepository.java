package socialmedia.repository.Hashtag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import socialmedia.model.Hashtag; // Đảm bảo import đúng model Hashtag
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    // Hàm tìm hashtag theo tên (để tránh tạo trùng)
    Optional<Hashtag> findByName(String name);
}