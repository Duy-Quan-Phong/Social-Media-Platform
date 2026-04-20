package com.codegym.socialmedia.repository.post;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostReaction;
import com.codegym.socialmedia.model.social_action.PostReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, PostReactionId> {

    Optional<PostReaction> findByPostAndUser(Post post, User user);

    void deleteByPostAndUser(Post post, User user);

    @Query("SELECT r.reactionType, COUNT(r) FROM PostReaction r WHERE r.post = :post GROUP BY r.reactionType")
    List<Object[]> countByReactionType(@Param("post") Post post);

    @Query("SELECT r.post.id, r.reactionType, COUNT(r) FROM PostReaction r WHERE r.post IN :posts GROUP BY r.post.id, r.reactionType")
    List<Object[]> countByReactionTypeForPosts(@Param("posts") List<Post> posts);

    @Query("SELECT r FROM PostReaction r WHERE r.user = :user AND r.post IN :posts")
    List<PostReaction> findByUserAndPosts(@Param("user") User user, @Param("posts") List<Post> posts);
}
