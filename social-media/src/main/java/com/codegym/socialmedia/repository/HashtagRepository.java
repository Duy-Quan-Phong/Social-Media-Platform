package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.social_action.Hashtag;
import com.codegym.socialmedia.model.social_action.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    @Query("SELECT p FROM Post p JOIN p.hashtags h WHERE h.name = :tag AND p.isDeleted = false AND p.privacyLevel = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Post> findPublicPostsByHashtag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT h.name, COUNT(p) as cnt FROM Post p JOIN p.hashtags h WHERE p.isDeleted = false GROUP BY h.name ORDER BY cnt DESC")
    List<Object[]> findTrendingHashtags(Pageable pageable);
}
