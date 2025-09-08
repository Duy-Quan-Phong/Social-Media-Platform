// PostLikeRepository.java
package com.codegym.socialmedia.repository.post;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.LikePost;
import com.codegym.socialmedia.model.social_action.LikePostId;
import com.codegym.socialmedia.model.social_action.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<LikePost, LikePostId> {

    // Tìm like của user cho post cụ thể
    Optional<LikePost> findByPostAndUser(Post post, User user);

    // Kiểm tra user đã like post chưa
//    boolean existsByPostAndUser(Post post, User user);

    // Đếm số likes của post
    int countByPost(Post post);

    // Lấy danh sách users đã like post
    @Query("SELECT pl.user FROM LikePost pl WHERE pl.post = :post")
    List<User> findUsersWhoLikedPost(@Param("post") Post post);

    // Xóa like
    void deleteByPostAndUser(Post post, User user);
}
