package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.Reel.Reel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface IReelRepository extends JpaRepository<Reel, Long> {
    // tìm kiếm theo hashtag của reel (thuật toán Content-Based Filtering)
    @Query("SELECT r from Reel r where r.hashtags like %:hashtag%")
    List<Reel> findByHashtag(@Param("hashtag") String hashtag);

    // tìm kiếm video của 1 người dùng cu thể
    List<Reel> findByUserId(Long userId);

    //phương thức lấy video random (dùng cho đề xuất ban đầu)
    @Query(value = "select * from reels order by rand() limit :limit", nativeQuery = true)
    List<Reel> findRandomReels(@Param("limit") int limit);
}

