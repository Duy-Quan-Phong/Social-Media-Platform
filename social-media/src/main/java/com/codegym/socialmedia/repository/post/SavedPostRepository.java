package com.codegym.socialmedia.repository.post;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.SavedPost;
import com.codegym.socialmedia.model.social_action.SavedPostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, SavedPostId> {

    boolean existsByPostAndUser(Post post, User user);

    void deleteByPostAndUser(Post post, User user);

    Page<SavedPost> findByUserOrderBySavedAtDesc(User user, Pageable pageable);

    @Query("SELECT sp.post.id FROM SavedPost sp WHERE sp.user = :user AND sp.post IN :posts")
    List<Long> findSavedPostIdsByUserAndPosts(@Param("user") User user, @Param("posts") List<Post> posts);
}
