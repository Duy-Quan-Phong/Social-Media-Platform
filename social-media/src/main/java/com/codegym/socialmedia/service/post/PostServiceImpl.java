package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.component.CloudinaryService;
import com.codegym.socialmedia.component.PrivacyUtils;
import com.codegym.socialmedia.dto.PollDto;
import com.codegym.socialmedia.dto.post.PostCreateDto;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.dto.post.PostUpdateDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.*;
import com.codegym.socialmedia.repository.HashtagRepository;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.repository.post.PostCommentRepository;
import com.codegym.socialmedia.repository.post.PostLikeRepository;
import com.codegym.socialmedia.repository.post.PostReactionRepository;
import com.codegym.socialmedia.repository.post.PostRepository;
import com.codegym.socialmedia.repository.post.SavedPostRepository;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.notification.PostMessage;
import com.codegym.socialmedia.service.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class PostServiceImpl implements PostService {
    @Autowired
    private IUserRepository userRepository;
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
    private SavedPostRepository savedPostRepository;

    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PollService pollService;

    @Override
    public Post createPost(PostCreateDto dto, User user) {
        Post post = new Post();
        post.setUser(user);
        post.setContent(dto.getContent());
        post.setPrivacyLevel(dto.getPrivacyLevel());

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

        Post saved = postRepository.save(post);
        saved.setHashtags(parseAndSaveHashtags(saved.getContent()));
        return postRepository.save(saved);
    }

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
        post.setHashtags(parseAndSaveHashtags(dto.getContent()));
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
        return convertPageWithBatchStats(posts, currentUser);
    }

    @Override
    public Page<PostDisplayDto> getPostsByUser(User targetUser, User currentUser, Pageable pageable) {
        Page<Post> posts;
        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            posts = postRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(targetUser, pageable);
        } else {
            posts = postRepository.findVisiblePostsByUser(targetUser, currentUser, pageable);
        }
        return convertPageWithBatchStats(posts, currentUser);
    }

    @Override
    public Page<PostDisplayDto> getPublicPostsByUser(User targetUser, User currentUser, Pageable pageable) {
        Page<Post> posts = postRepository.findVisiblePostsByUser(targetUser, currentUser, pageable);
        return convertPageWithBatchStats(posts, null);
    }

    @Override
    public Page<PostDisplayDto> searchUserPosts(User user, User currentUser, String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.searchPostsOnProfile(user, currentUser, keyword, pageable);
        return convertPageWithBatchStats(posts, currentUser);
    }

    @Override
    public Page<PostDisplayDto> searchPublicPosts(String keyword, User currentUser, Pageable pageable) {
        Page<Post> posts = postRepository.searchPublicPosts(keyword, pageable);
        return convertPageWithBatchStats(posts, currentUser);
    }

    // 1 query per stat type instead of N queries
    private Page<PostDisplayDto> convertPageWithBatchStats(Page<Post> posts, User currentUser) {
        List<Post> postList = posts.getContent();
        if (postList.isEmpty()) return posts.map(p -> convertToDisplayDto(p, currentUser));

        Map<Long, Long> likeCounts = postRepository.countLikesByPosts(postList).stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0], row -> (Long) row[1]));
        Map<Long, Long> commentCounts = postRepository.countCommentsByPosts(postList).stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0], row -> (Long) row[1]));

        Set<Long> savedPostIds = new HashSet<>();
        if (currentUser != null) {
            savedPostIds.addAll(savedPostRepository.findSavedPostIdsByUserAndPosts(currentUser, postList));
        }

        Map<Long, Map<String, Integer>> reactionCountsMap = new HashMap<>();
        postReactionRepository.countByReactionTypeForPosts(postList).forEach(row -> {
            Long pid = (Long) row[0];
            String rtype = ((ReactionType) row[1]).name();
            Long count = (Long) row[2];
            reactionCountsMap.computeIfAbsent(pid, k -> new LinkedHashMap<>()).put(rtype, count.intValue());
        });

        Map<Long, String> userReactions = new HashMap<>();
        if (currentUser != null) {
            postReactionRepository.findByUserAndPosts(currentUser, postList)
                .forEach(r -> userReactions.put(r.getPost().getId(), r.getReactionType().name()));
        }

        return posts.map(post -> {
            PostDisplayDto dto = convertToDisplayDto(post, currentUser);
            dto.setLikesCount(likeCounts.getOrDefault(post.getId(), 0L).intValue());
            dto.setCommentsCount(commentCounts.getOrDefault(post.getId(), 0L).intValue());
            dto.setSavedByCurrentUser(savedPostIds.contains(post.getId()));
            dto.setReactionCounts(reactionCountsMap.getOrDefault(post.getId(), new LinkedHashMap<>()));
            dto.setCurrentUserReaction(userReactions.get(post.getId()));
            return dto;
        });
    }

    @Override
    public boolean toggleSavePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (savedPostRepository.existsByPostAndUser(post, user)) {
            savedPostRepository.deleteByPostAndUser(post, user);
            return false;
        } else {
            SavedPost saved = new SavedPost();
            SavedPostId savedId = new SavedPostId(user.getId(), postId);
            saved.setId(savedId);
            saved.setUser(user);
            saved.setPost(post);
            savedPostRepository.save(saved);
            return true;
        }
    }

    @Override
    public Page<PostDisplayDto> getSavedPosts(User user, Pageable pageable) {
        Page<SavedPost> savedPosts = savedPostRepository.findByUserOrderBySavedAtDesc(user, pageable);
        Page<Post> posts = savedPosts.map(SavedPost::getPost);
        return convertPageWithBatchStats(posts, user);
    }

    @Override
    public boolean toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        LikePostId likePostId = getLikeStatusId(postId, user.getId());
        boolean isLiked = postLikeRepository.findById(likePostId).isPresent();

        if (isLiked) {
            postLikeRepository.deleteByPostAndUser(post, user);
            postMessage.notifyLikeStatusChanged(postId, getLikeCount(post), false, user.getUsername());
            return false;
        } else {
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
            return true;
        }
    }

    @Override
    public String toggleReaction(Long postId, User user, String reactionTypeStr) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        ReactionType reactionType = ReactionType.valueOf(reactionTypeStr.toUpperCase());
        java.util.Optional<PostReaction> existing = postReactionRepository.findByPostAndUser(post, user);
        LikePostId likeId = getLikeStatusId(postId, user.getId());

        if (existing.isPresent()) {
            if (existing.get().getReactionType() == reactionType) {
                // Same reaction → remove it
                postReactionRepository.delete(existing.get());
                postLikeRepository.deleteByPostAndUser(post, user);
                postMessage.notifyLikeStatusChanged(postId, getLikeCount(post), false, user.getUsername());
                return null;
            } else {
                // Different reaction → update type, LikePost stays
                existing.get().setReactionType(reactionType);
                postReactionRepository.save(existing.get());
                return reactionType.name();
            }
        } else {
            // New reaction
            PostReaction reaction = new PostReaction();
            reaction.setId(new PostReactionId(user.getId(), postId));
            reaction.setUser(user);
            reaction.setPost(post);
            reaction.setReactionType(reactionType);
            reaction.setReactedAt(java.time.LocalDateTime.now());
            postReactionRepository.save(reaction);

            if (!postLikeRepository.findById(likeId).isPresent()) {
                LikePost like = new LikePost();
                like.setPost(post);
                like.setUser(user);
                like.setId(likeId);
                postLikeRepository.save(like);
                notificationService.notify(user.getId(), post.getUser().getId(),
                        Notification.NotificationType.LIKE_POST,
                        Notification.ReferenceType.POST, postId);
            }
            postMessage.notifyLikeStatusChanged(postId, getLikeCount(post), true, user.getUsername());
            return reactionType.name();
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
        boolean isLiked = false;
        if (currentUser != null) {
            LikePostId likePostId = getLikeStatusId(post.getId(), currentUser.getId());
            isLiked = postLikeRepository.findById(likePostId).isPresent();
        }

        boolean canEdit = currentUser != null &&
                post.getUser().getId().equals(currentUser.getId());

        boolean canDelete = canEdit;

        boolean isSaved = currentUser != null &&
                savedPostRepository.existsByPostAndUser(post, currentUser);

        PostDisplayDto dto = new PostDisplayDto(post, isLiked, canEdit, canDelete);
        dto.setSavedByCurrentUser(isSaved);

        if (currentUser == null) {
            dto.setCanComment(false);
        } else if (currentUser.isAdmin()) {
            dto.setCanComment(true);
        } else {
            Friendship.FriendshipStatus friendshipStatus =
                friendshipService.getFriendshipStatus(post.getUser(), currentUser);
            boolean isFriend = (friendshipStatus == Friendship.FriendshipStatus.ACCEPTED);
            dto.setCanComment(PrivacyUtils.canView(currentUser, post.getUser(), post.getPrivacyCommentLevel(), isFriend));
        }

        dto.setLikesCount(getLikeCount(post));
        dto.setCommentsCount(countCommentsByPost(post));

        Map<String, Integer> reactionCounts = new LinkedHashMap<>();
        postReactionRepository.countByReactionType(post)
            .forEach(row -> reactionCounts.put(((ReactionType) row[0]).name(), ((Long) row[1]).intValue()));
        dto.setReactionCounts(reactionCounts);

        if (currentUser != null) {
            postReactionRepository.findByPostAndUser(post, currentUser)
                .ifPresent(r -> dto.setCurrentUserReaction(r.getReactionType().name()));
        }

        dto.setPoll(pollService.getPollDto(post.getId(), currentUser));

        return dto;
    }

    @Override
    public int getLikeCount(Post post) {
        return postLikeRepository.countByPost(post);
    }

    @Autowired
    private PostCommentRepository postCommentRepository;

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

    // ── Hashtag helpers ────────────────────────────────────────────────────────

    private java.util.Set<Hashtag> parseAndSaveHashtags(String content) {
        java.util.Set<Hashtag> result = new HashSet<>();
        if (content == null) return result;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("#([\\w\\u00C0-\\u024F]+)").matcher(content);
        while (m.find()) {
            String tag = m.group(1).toLowerCase();
            Hashtag ht = hashtagRepository.findByName(tag)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(null, tag)));
            result.add(ht);
        }
        return result;
    }

    @Override
    public Page<PostDisplayDto> getPostsByHashtag(String tag, User currentUser, Pageable pageable) {
        Page<Post> posts = hashtagRepository.findPublicPostsByHashtag(tag.toLowerCase(), pageable);
        return convertPageWithBatchStats(posts, currentUser);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getTrendingHashtags(int limit) {
        return hashtagRepository.findTrendingHashtags(org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(row -> java.util.Map.of("name", row[0], "count", row[1]))
                .toList();
    }

    // ── Share post ────────────────────────────────────────────────────────────

    @Override
    public Post sharePost(Long originalPostId, User user, String comment) {
        Post original = postRepository.findById(originalPostId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Post share = new Post();
        share.setUser(user);
        share.setContent(comment != null ? comment : "");
        share.setPrivacyLevel(original.getPrivacyLevel());
        share.setPrivacyCommentLevel(original.getPrivacyCommentLevel());
        share.setSharedFrom(original);

        Post saved = postRepository.save(share);

        notificationService.notify(user.getId(), original.getUser().getId(),
                Notification.NotificationType.SHARED_POST,
                Notification.ReferenceType.POST, original.getId());

        return saved;
    }
}
