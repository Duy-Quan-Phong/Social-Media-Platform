package socialmedia.repository.post;

import socialmedia.model.account.User;
import socialmedia.model.social_action.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // BASIC methods - không có complex queries

    //  Find posts by user with pagination
    Page<Post> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    //  Find public posts by user (Nếu là người lạ), thêm post cho bạn bè - THÊM METHOD NÀY
    @Query("""
            SELECT p FROM Post p
            WHERE p.isDeleted = false
              AND p.user = :owner
              AND (
                    p.privacyLevel = socialmedia.model.PrivacyLevel.PUBLIC
                 OR :viewer = :owner
                 OR (
                      p.privacyLevel =  socialmedia.model.PrivacyLevel.FRIENDS
                      AND EXISTS (
                            SELECT 1 FROM Friendship f
                            WHERE f.status = socialmedia.model.social_action.Friendship.FriendshipStatus.ACCEPTED
                              AND (
                                    (f.requester = :viewer AND f.addressee = :owner)
                                 OR (f.requester = :owner  AND f.addressee = :viewer)
                              )
                      )
                    )
              )
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findVisiblePostsByUser(@Param("owner") User owner,
                                      @Param("viewer") User viewer,
                                      Pageable pageable);

    // 4. Count posts by user
    long countByUserAndIsDeletedFalse(User user);

    // 5. Find by ID and user
    Optional<Post> findByIdAndUser(Long id, User user);

    // 6. Search posts by user and content - THÊM METHOD NÀY
    @Query("""
                SELECT p FROM Post p
                WHERE p.isDeleted = false
                  AND p.user = :owner
                  AND LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  AND (
                        :viewer = :owner
                        OR p.privacyLevel = 'PUBLIC'
                        OR (
                            p.privacyLevel = 'FRIENDS'
                            AND EXISTS (
                                SELECT f FROM Friendship f
                                WHERE f.status = 'ACCEPTED'
                                  AND (
                                      (f.requester = :viewer AND f.addressee = :owner)
                                      OR
                                      (f.addressee = :viewer AND f.requester = :owner)
                                  )
                            )
                        )
                      )
                ORDER BY p.createdAt DESC
            """)
    Page<Post> searchPostsOnProfile(@Param("owner") User owner,
                                    @Param("viewer") User viewer,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);



    @Query(value = """
    SELECT p.* FROM posts p
    LEFT JOIN post_recommendations pr ON p.id = pr.post_id AND pr.user_id = :currentUser
    WHERE p.is_deleted = FALSE
      AND (
           p.privacy_level = 'PUBLIC'
        OR (p.privacy_level = 'FRIENDS' AND 
            EXISTS (
                SELECT 1 FROM friendships f 
                WHERE f.status = 'ACCEPTED'
                  AND ((f.requester_id = :currentUser AND f.addressee_id = p.user_id)
                    OR (f.addressee_id = :currentUser AND f.requester_id = p.user_id))
            )
        )
        OR (p.user_id = :currentUser)
      )
    ORDER BY 
       (CASE WHEN p.user_id = :currentUser THEN 1 ELSE 0 END) DESC, 
       pr.score DESC,                                               
       p.created_at DESC                                            
    """,
            countQuery = """
    SELECT count(*) FROM posts p 
    WHERE p.is_deleted = FALSE
      AND (
           p.privacy_level = 'PUBLIC'
        OR (p.privacy_level = 'FRIENDS' AND 
            EXISTS (
                SELECT 1 FROM friendships f 
                WHERE f.status = 'ACCEPTED'
                  AND ((f.requester_id = :currentUser AND f.addressee_id = p.user_id)
                    OR (f.addressee_id = :currentUser AND f.requester_id = p.user_id))
            )
        )
        OR (p.user_id = :currentUser)
      )
    """,
            nativeQuery = true)
    Page<Post> findVisiblePosts(@Param("currentUser") Long currentUser, Pageable pageable);


    // 8. Find posts by user list (for friends' posts) - THÊM METHOD NÀY
    @Query("SELECT p FROM Post p WHERE p.user IN :users AND p.isDeleted = false " +
            "AND (p.privacyLevel = 'PUBLIC' OR p.privacyLevel = 'FRIENDS') " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsByUserIn(@Param("users") List<User> users, Pageable pageable);
    // Lấy tất cả ảnh công khai
    @Query("SELECT p.imageUrls FROM Post p " +
            "WHERE p.user = :profileOwner " +
            "AND p.privacyLevel = 'PUBLIC' " +
            "AND p.isDeleted = false " +
            "AND p.imageUrls IS NOT NULL")
    List<String> findPublicPhotos(@Param("profileOwner") User profileOwner);

    // Lấy ảnh theo phân quyền (public, friends, chính chủ)
    @Query("SELECT p.imageUrls FROM Post p " +
            "LEFT JOIN Friendship f ON ((f.requester = :viewer AND f.addressee = p.user) " +
            "OR (f.addressee = :viewer AND f.requester = p.user)) " +
            "WHERE p.user = :profileOwner " +
            "AND p.isDeleted = false " +
            "AND p.imageUrls IS NOT NULL " +
            "AND (p.privacyLevel = 'PUBLIC' " +
            "     OR (p.privacyLevel = 'FRIENDS' AND f.status = 'ACCEPTED') " +
            "     OR p.user = :viewer)")
    List<String> findVisiblePhotos(@Param("profileOwner") User profileOwner,
                                   @Param("viewer") User viewer);

    // --- THÊM ĐOẠN NÀY VÀO ---

    @Query(value = """
        SELECT p.* FROM posts p
        JOIN post_hashtags ph ON p.id = ph.post_id
        JOIN hashtags h ON ph.hashtag_id = h.id
        WHERE h.name = :tagName
          AND p.is_deleted = FALSE
          AND (
               p.privacy_level = 'PUBLIC'
            OR (p.privacy_level = 'FRIENDS' AND 
                EXISTS (
                    SELECT 1 FROM friendships f 
                    WHERE f.status = 'ACCEPTED'
                      AND ((f.requester_id = :currentUser AND f.addressee_id = p.user_id)
                        OR (f.addressee_id = :currentUser AND f.requester_id = p.user_id))
                )
            )
            OR (p.user_id = :currentUser)
          )
        ORDER BY p.created_at DESC
    """,
            countQuery = """
        SELECT count(*) FROM posts p
        JOIN post_hashtags ph ON p.id = ph.post_id
        JOIN hashtags h ON ph.hashtag_id = h.id
        WHERE h.name = :tagName
          AND p.is_deleted = FALSE
          AND (
               p.privacy_level = 'PUBLIC'
            OR (p.privacy_level = 'FRIENDS' AND 
                EXISTS (
                    SELECT 1 FROM friendships f 
                    WHERE f.status = 'ACCEPTED'
                      AND ((f.requester_id = :currentUser AND f.addressee_id = p.user_id)
                        OR (f.addressee_id = :currentUser AND f.requester_id = p.user_id))
                )
            )
            OR (p.user_id = :currentUser)
          )
    """,
            nativeQuery = true)
    Page<Post> findPostsByHashtag(@Param("tagName") String tagName,
                                  @Param("currentUser") Long currentUser,
                                  Pageable pageable);
}