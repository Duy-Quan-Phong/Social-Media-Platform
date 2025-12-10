package com.codegym.socailmedia.service.post;

import com.codegym.socailmedia.dto.post.PostCreateDto;
import com.codegym.socailmedia.dto.post.PostDisplayDto;
import com.codegym.socailmedia.dto.post.PostUpdateDto;
import com.codegym.socailmedia.model.account.User;
import com.codegym.socailmedia.model.social_action.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PostService {

    // CRUD Operations
    Post createPost(PostCreateDto dto, User user);
    Post updatePost(Long postId, PostUpdateDto dto, User user);
    void deletePost(Long postId, User user);
    PostDisplayDto getPostById(Long postId, User currentUser);
    Post getPostById(long id);

    // Get posts for different contexts
    Page<PostDisplayDto> getPostsForNewsFeed(User currentUser, Pageable pageable);
    Page<PostDisplayDto> getPostsByUser(User targetUser, User currentUser, Pageable pageable);
    Page<PostDisplayDto> getPublicPostsByUser(User targetUser, User currentUser ,Pageable pageable);
    Page<PostDisplayDto> searchUserPosts(User user, User currentUser, String keyword, Pageable pageable);

    // --- THÊM DÒNG NÀY (Hàm tìm kiếm Hashtag) ---
    Page<PostDisplayDto> searchPostsByHashtag(String hashtag, User currentUser, Pageable pageable);

    // Like functionality
    boolean toggleLike(Long postId, User user);
    List<User> getUsersWhoLiked(Long postId);
    int getLikeCount(Post post); // Lưu ý: Chỗ này sửa lại tham số là Post cho khớp với impl
    int countCommentsByPost(Post post);
    List<String> getPhotosForProfile(User profileOwner, User viewer);

    // Utility
    long countUserPosts(User user);
}