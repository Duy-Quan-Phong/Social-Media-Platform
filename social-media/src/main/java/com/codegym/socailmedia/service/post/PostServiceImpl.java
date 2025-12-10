package com.codegym.socailmedia.service.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.codegym.socailmedia.component.CloudinaryService;
import com.codegym.socailmedia.component.PrivacyUtils;
import com.codegym.socailmedia.dto.post.PostCreateDto;
import com.codegym.socailmedia.dto.post.PostDisplayDto;
import com.codegym.socailmedia.dto.post.PostUpdateDto;
import com.codegym.socailmedia.model.Hashtag;
import com.codegym.socailmedia.model.PostHashtag; // Import Entity PostHashtag
import com.codegym.socailmedia.model.UserHashtagInterest;
import com.codegym.socailmedia.model.account.User;
import com.codegym.socailmedia.model.social_action.*;
import com.codegym.socailmedia.repository.Hashtag.HashtagRepository; // Sửa package cho đúng
import com.codegym.socailmedia.repository.Hashtag.PostHashtagRepository; // Sửa package cho đúng
import com.codegym.socailmedia.repository.UserRepository;
import com.codegym.socailmedia.repository.post.PostCommentRepository;
import com.codegym.socailmedia.repository.post.PostLikeRepository;
import com.codegym.socailmedia.repository.post.PostRepository;
import com.codegym.socailmedia.service.friend_ship.FriendshipService;
import com.codegym.socailmedia.service.notification.NotificationService;
import com.codegym.socailmedia.service.notification.PostMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    // ... các autowired cũ ...
    @Autowired
    private com.codegym.socailmedia.repository.Hashtag.UserHashtagInterestRepository interestRepo; // <-- THÊM CÁI NÀY
    @Autowired
    private HashtagRepository hashtagRepo;
    @Autowired
    private PostHashtagRepository postHashtagRepo;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostMessage postMessage;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private PostCommentRepository postCommentRepository;

    // --- HÀM TẠO BÀI VIẾT (ĐÃ HỢP NHẤT LOGIC) ---
    @Override
    public Post createPost(PostCreateDto dto, User user) {
        Post post = new Post();
        post.setUser(user);
        post.setContent(dto.getContent());
        post.setPrivacyLevel(dto.getPrivacyLevel());
        // Mặc định privacy comment nếu DTO không có
        post.setPrivacyCommentLevel(dto.getPrivacyLevel());

        // 1. Upload ảnh (Logic cũ)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : dto.getImages()) {
                if (!image.isEmpty()) {
                    String imageUrl = cloudinaryService.upload(image);
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                    }
                }
            }
            post.setImageUrls(convertListToJson(imageUrls));
        }

        // 2. Lưu bài viết trước để có ID
        post = postRepository.save(post);

        // 3. Xử lý Hashtag (Logic mới thêm vào)
        if (post.getContent() != null && !post.getContent().isEmpty()) {
            processHashtags(post);
        }

        return post;
    }

    // --- LOGIC HASHTAG ---
    private void processHashtags(Post post) {
        Set<String> tagNames = extractHashtags(post.getContent());

        for (String tagName : tagNames) {
            // A. Tìm hoặc Tạo mới Hashtag
            Hashtag hashtag = hashtagRepo.findByName(tagName)
                    .orElseGet(() -> {
                        Hashtag newTag = new Hashtag();
                        newTag.setName(tagName);
                        return hashtagRepo.save(newTag);
                    });

            // B. Lưu vào bảng nối (Post - Hashtag)
            // Sử dụng Entity PostHashtag chuẩn JPA
            try {
                PostHashtag link = new PostHashtag(post, hashtag);
                postHashtagRepo.save(link);
            } catch (Exception e) {
                // Bỏ qua lỗi nếu đã tồn tại (để tránh lỗi duplicate key)
            }
        }
    }

    private Set<String> extractHashtags(String content) {
        Set<String> hashtags = new HashSet<>();
        // Regex bắt chữ sau dấu # (hỗ trợ tiếng Việt và số)
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase());
        }
        return hashtags;
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---

    @Override
    public Post updatePost(Long postId, PostUpdateDto dto, User user) {
        Post post = postRepository.findByIdAndUser(postId, user)
                .orElseThrow(() -> new RuntimeException("Post not found or access denied"));

        post.setContent(dto.getContent());
        post.setPrivacyLevel(dto.getPrivacyLevel());
        post.setPrivacyCommentLevel(dto.getCommentPrivacyLevel());

        List<String> newImageUrls = new ArrayList<>();
        if (dto.getExistingImages() != null) {
            newImageUrls.addAll(dto.getExistingImages());
        }
        if (dto.getImagesToDelete() != null) {
            newImageUrls.removeAll(dto.getImagesToDelete());
        }
        if (dto.getNewImages() != null && !dto.getNewImages().isEmpty()) {
            for (MultipartFile newImage : dto.getNewImages()) {
                if (!newImage.isEmpty()) {
                    String imageUrl = cloudinaryService.upload(newImage);
                    if (imageUrl != null) {
                        newImageUrls.add(imageUrl);
                    }
                }
            }
        }
        post.setImageUrls(convertListToJson(newImageUrls));

        // Cập nhật lại Hashtag khi sửa bài (Xóa cũ thêm mới - Tạm thời chỉ thêm mới cho đơn giản)
        if (post.getContent() != null) {
            processHashtags(post);
        }

        return postRepository.save(post);
    }

    @Override
    public void deletePost(Long postId, User user) {
        Post post = postRepository.findByIdAndUser(postId, user)
                .orElseThrow(() -> new RuntimeException("Post not found or access denied"));
        post.setDeleted(true);
        postRepository.save(post);
    }

    @Override
    public PostDisplayDto getPostById(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToDisplayDto(post, currentUser);
    }

    @Override
    public Post getPostById(long id) {
        return postRepository.findById(id).orElse(null);
    }

    @Override
    public Page<PostDisplayDto> getPostsForNewsFeed(User currentUser, Pageable pageable) {
        Page<Post> posts = postRepository.findVisiblePosts(currentUser.getId(), pageable);
        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }

    @Override
    public Page<PostDisplayDto> getPostsByUser(User targetUser, User currentUser, Pageable pageable) {
        Page<Post> posts;
        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            posts = postRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(targetUser, pageable);
        } else {
            posts = postRepository.findVisiblePostsByUser(targetUser, currentUser, pageable);
        }
        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }

    @Override
    public Page<PostDisplayDto> getPublicPostsByUser(User targetUser, User currentUser, Pageable pageable) {
        Page<Post> posts = postRepository.findVisiblePostsByUser(targetUser, currentUser, pageable);
        return posts.map(post -> convertToDisplayDto(post, null));
    }

    @Override
    public Page<PostDisplayDto> searchUserPosts(User user, User currentUser, String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.searchPostsOnProfile(user, currentUser, keyword, pageable);
        return posts.map(post -> convertToDisplayDto(post, user));
    }

    @Override
    public boolean toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        LikePostId likePostId = getLikeStatusId(postId, user.getId());
        boolean isLiked = postLikeRepository.findById(likePostId).isPresent();

        if (isLiked) {
            // --- TRƯỜNG HỢP BỎ LIKE ---
            postLikeRepository.deleteByPostAndUser(post, user);
            postMessage.notifyLikeStatusChanged(postId, getLikeCount(post), false, user.getUsername());

            // [THÊM DÒNG NÀY] Trừ điểm AI
            updateAiInterests(postId, user.getId(), false);

            return false;
        } else {
            // --- TRƯỜNG HỢP LIKE ---
            LikePost like = new LikePost();
            like.setPost(post);
            like.setUser(user);
            like.setId(likePostId);
            postLikeRepository.save(like);

            notificationService.notify(
                    user.getId(), post.getUser().getId(),
                    Notification.NotificationType.LIKE_POST,
                    Notification.ReferenceType.POST, postId);
            postMessage.notifyLikeStatusChanged(postId, getLikeCount(post), true, user.getUsername());

            // [THÊM DÒNG NÀY] Cộng điểm AI
            updateAiInterests(postId, user.getId(), true);

            return true;
        }
    }

    private static LikePostId getLikeStatusId(Long postId, Long userId) {
        LikePostId likeStatusId = new LikePostId();
        likeStatusId.setPostId(postId);
        likeStatusId.setUserId(userId);
        return likeStatusId;
    }

    @Override
    public List<User> getUsersWhoLiked(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return postLikeRepository.findUsersWhoLikedPost(post);
    }

    @Override
    public long countUserPosts(User user) {
        return postRepository.countByUserAndIsDeletedFalse(user);
    }

    private PostDisplayDto convertToDisplayDto(Post post, User currentUser) {
        LikePostId likePostId = getLikeStatusId(post.getId(), currentUser.getId());
        boolean isLiked = currentUser != null &&
                postLikeRepository.findById(likePostId).isPresent();

        boolean canEdit = currentUser != null &&
                post.getUser().getId().equals(currentUser.getId());

        boolean canDelete = canEdit;

        PostDisplayDto dto = new PostDisplayDto(post, isLiked, canEdit, canDelete);

        if (currentUser.isAdmin()) {
            dto.setCanComment(true);
        } else {
            Friendship.FriendshipStatus friendshipStatus =
                    friendshipService.getFriendshipStatus(post.getUser(), currentUser);
            boolean isFriend = (friendshipStatus == Friendship.FriendshipStatus.ACCEPTED);
            dto.setCanComment(PrivacyUtils.canView(currentUser, post.getUser(), post.getPrivacyCommentLevel(), isFriend));
        }

        dto.setLikesCount(getLikeCount(post));
        dto.setCommentsCount(countCommentsByPost(post));
        return dto;
    }

    @Override
    public int getLikeCount(Post post) {
        return postLikeRepository.countByPost(post);
    }

    @Override
    public int countCommentsByPost(Post post) {
        return postCommentRepository.countByPost(post);
    }

    public List<String> getPhotosForProfile(User profileOwner, User viewer) {
        if (viewer == null) {
            return postRepository.findPublicPhotos(profileOwner);
        }
        return postRepository.findVisiblePhotos(profileOwner, viewer);
    }

    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
    // --- LOGIC AI: Cập nhật điểm sở thích ---
    private void updateAiInterests(Long postId, Long userId, boolean isLiked) {
        try {
            // 1. Lấy các hashtag của bài viết
            List<Hashtag> hashtags = postHashtagRepo.findHashtagsByPostId(postId);
            if (hashtags == null || hashtags.isEmpty()) return;

            // 2. Like thì +5 điểm, Bỏ like thì -5 điểm
            double scoreDelta = isLiked ? 5.0 : -5.0;

            for (Hashtag tag : hashtags) {
                // Tìm xem user đã có điểm với hashtag này chưa
                UserHashtagInterest interest = interestRepo.findByUserIdAndHashtagId(userId, tag.getId())
                        .orElse(new UserHashtagInterest(userId, tag.getId(), 0.0));

                // Cộng điểm mới
                double newScore = interest.getInterestScore() + scoreDelta;
                if (newScore < 0) newScore = 0; // Không để âm

                interest.setInterestScore(newScore);
                interest.setLastInteraction(java.time.LocalDateTime.now());

                interestRepo.save(interest);
            }
            System.out.println("✅ AI Update: Đã cập nhật điểm cho User " + userId);
        } catch (Exception e) {
            System.err.println("⚠️ AI Update Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // --- HÀM TÌM KIẾM HASHTAG (Bổ sung để hết lỗi) ---
    @Override
    public Page<PostDisplayDto> searchPostsByHashtag(String hashtag, User currentUser, Pageable pageable) {
        // 1. Làm sạch từ khóa (Bỏ dấu # nếu có)
        String cleanTag = hashtag.replace("#", "").trim().toLowerCase();

        // 2. Gọi Repository để tìm bài viết
        // (Lưu ý: currentUserId cần xử lý null nếu user chưa đăng nhập, nhưng ở controller ta đã bắt buộc đăng nhập rồi)
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        Page<Post> posts = postRepository.findPostsByHashtag(cleanTag, currentUserId, pageable);

        // 3. Convert sang DTO để hiển thị
        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }
}