package com.codegym.socailmedia.repository.Hashtag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.codegym.socailmedia.model.Hashtag; // Đảm bảo import đúng model Hashtag
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    // Hàm tìm hashtag theo tên (để tránh tạo trùng)
    Optional<Hashtag> findByName(String name);
}