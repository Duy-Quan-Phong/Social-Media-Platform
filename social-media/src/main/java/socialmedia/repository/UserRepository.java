package socialmedia.repository;

import socialmedia.model.account.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // <-- QUAN TRỌNG: Đã import Optional

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // --- CẬP NHẬT: Trả về Optional để tránh lỗi NullPointerException ---
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = """
        SELECT * FROM users u
        WHERE u.id != :currentUserId
        AND u.is_active = 1  
        AND u.id NOT IN (
            SELECT f.requester_id FROM friendships f WHERE f.addressee_id = :currentUserId
            UNION
            SELECT f.addressee_id FROM friendships f WHERE f.requester_id = :currentUserId
        )
        ORDER BY RAND() 
        LIMIT 12
    """, nativeQuery = true)
    List<User> findRandomStrangers(@Param("currentUserId") Long currentUserId);

    // --- CÁC QUERY CŨ GIỮ NGUYÊN ---

    @Query("""
        SELECT u FROM User u
        WHERE (LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    List<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE u.id <> :currentUserId
          AND (LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    List<User> searchUsersExcludeCurrent(@Param("keyword") String keyword,
                                         @Param("currentUserId") Long currentUserId,
                                         Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE (LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND u.id <> :currentUserId
          AND NOT EXISTS (
            SELECT 1 FROM ConversationParticipant cp
            WHERE cp.conversation.id = :conversationId
              AND cp.user.id = u.id
              AND cp.isActive = true
          )
    """)
    List<User> searchUsersNotInConversation(@Param("keyword") String keyword,
                                            @Param("conversationId") Long conversationId,
                                            @Param("currentUserId") Long currentUserId,
                                            Pageable pageable);

    Page<User> findByIdNot(Long id, Pageable pageable);

    @Query("SELECT u FROM User u " +
            "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);
}