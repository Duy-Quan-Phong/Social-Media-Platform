package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // Stories of a single user that haven't expired
    List<Story> findByUserAndExpiresAtAfterOrderByCreatedAtDesc(User user, LocalDateTime now);

    // Active stories from a list of friends
    @Query("SELECT s FROM Story s WHERE s.user IN :friends AND s.expiresAt > :now ORDER BY s.user.id, s.createdAt DESC")
    List<Story> findActiveStoriesFromFriends(@Param("friends") List<User> friends, @Param("now") LocalDateTime now);

    // Delete expired stories
    void deleteByExpiresAtBefore(LocalDateTime now);
}
