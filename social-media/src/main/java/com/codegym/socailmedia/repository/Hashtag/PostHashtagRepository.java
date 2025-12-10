package com.codegym.socailmedia.repository.Hashtag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.codegym.socailmedia.model.Hashtag;
import com.codegym.socailmedia.model.PostHashtag;   // <--- Import đúng
import com.codegym.socailmedia.model.PostHashtagId; // <--- Import đúng
import java.util.List;

@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtagId> {
    // ^^^ QUAN TRỌNG: Sửa Generic type ở dòng trên

    @Query(value = """
        SELECT h.* FROM hashtags h
        JOIN post_hashtags ph ON h.id = ph.hashtag_id
        WHERE ph.post_id = :postId
    """, nativeQuery = true)
    List<Hashtag> findHashtagsByPostId(@Param("postId") Long postId);
}