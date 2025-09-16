package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.comment.DisplayCommentDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.*;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.repository.comment.LikeCommentRepository;
import com.codegym.socialmedia.repository.comment.MentionRepository;
import com.codegym.socialmedia.repository.post.PostCommentRepository;
import com.codegym.socialmedia.repository.post.PostRepository;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.notification.NotificationService;
import com.codegym.socialmedia.service.notification.PostMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.codegym.socialmedia.dto.comment.DisplayCommentDTO.mapToDTO;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommentServiceImpl implements PostCommentService {
    private final IUserRepository userRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final PostMessage postMessage;
    private final NotificationService notificationService;
    private final FriendshipService friendshipService;
    private final MentionRepository mentionRepository;

    @Override
    public PostComment addComment(Long postId, User user, String content, List<Long> mentionIds) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User postOwner = post.getUser();

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        comment.setDeleted(false);
        comment.setCreatedAt(LocalDateTime.now());

        try {
            // ✅ Lưu comment trước
            PostComment savedComment = postCommentRepository.save(comment);

            // ✅ Lưu mentions nếu có
            if (mentionIds != null && !mentionIds.isEmpty()) {
                for (long mentionedUserId : mentionIds) {
                    User u = userRepository.findById(mentionedUserId).orElseThrow(() -> new RuntimeException("User not found"));
                    CommentMention cm = new CommentMention();
                    cm.setComment(savedComment);
                    cm.setMentionedUser(u);
                    mentionRepository.save(cm);

                    notificationService.notify(
                            user.getId(),
                            mentionedUserId,
                            Notification.NotificationType.MENTION_COMMENT,
                            Notification.ReferenceType.COMMENT,
                            savedComment.getId()
                    );
                }
            }

            // ✅ Cập nhật số lượng comment cho post
            postMessage.notifyCommentStatusChanged(
                    postId,
                    postCommentRepository.countByPost(post)
            );

            // ✅ Gửi thông báo cho chủ bài viết
            if (!user.getId().equals(postOwner.getId())) {
                notificationService.notify(
                        user.getId(),
                        postOwner.getId(),
                        Notification.NotificationType.COMMENT_POST,
                        Notification.ReferenceType.COMMENT,
                        savedComment.getId()
                );
            }

            return savedComment;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public PostComment updateComment(Long commentId, User currentUser, String newContent) {
        PostComment comment = postCommentRepository.findByIdAndUser(commentId, currentUser)
                .orElseThrow(() -> new RuntimeException("Comment không tồn tại hoặc bạn không có quyền sửa"));

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        return postCommentRepository.save(comment);
    }

    private List<User> getMentionedUsersByCommentId(Long commentId) {
        return mentionRepository.getUsersByCommentId(commentId);
    }

    @Override
    public Page<DisplayCommentDTO> getCommentsByPost(Long postId, User currentUser, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<PostComment> comments = postCommentRepository.findRecentCommentsByPost(post, pageable);

        return comments.map(comment -> mapToDTO(comment, currentUser, getMentionedUsersByCommentId(comment.getId()),friendshipService));
    }

    @Override
    public PostComment deleteComment(Long commentId, User currentUser) {
        PostComment comment = postCommentRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getUser().getId().equals(currentUser.getId())) {
            return null;
        }

        try {
            Post post = comment.getPost();
            postCommentRepository.delete(comment);
            postMessage.notifyCommentStatusChanged(post.getId(), postCommentRepository.countByPost(post));
            return comment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Optional<PostComment> getCommentById(Long commentId) {
        return postCommentRepository.findById(commentId);
    }

    /**
     * Toggle like/unlike comment
     */
    @Override
    public boolean toggleLikeComment(Long commentId, User currentUser) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        LikeCommentId likeId = new LikeCommentId();
        likeId.setCommentId(commentId);
        likeId.setUserId(currentUser.getId());

        Optional<LikeComment> existingLike = likeCommentRepository.findById(likeId);

        boolean likedByCurrentUser;

        if (existingLike.isPresent()) {
            // unlike
            likeCommentRepository.delete(existingLike.get());
            likedByCurrentUser = false;
        } else {
            // like mới
            LikeComment like = new LikeComment();
            like.setId(likeId);
            like.setUser(currentUser);
            like.setComment(comment);
            likeCommentRepository.save(like);
            likedByCurrentUser = true;
            notificationService.notify(currentUser.getId(), comment.getUser().getId(),
                    Notification.NotificationType.LIKE_COMMENT, Notification.ReferenceType.COMMENT, commentId);
        }

        postMessage.notifyCommentLikeChanged(
                comment.getId(),
                likeCommentRepository.countByComment(comment),
                likedByCurrentUser, currentUser.getUsername()
        );


        return likedByCurrentUser;
    }

    @Override
    public DisplayCommentDTO replyToComment(Long parentCommentId, User currentUser, String content) {
        PostComment parent = postCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        PostComment reply = new PostComment();
        reply.setContent(content);
        reply.setUser(currentUser);
        reply.setPost(parent.getPost());
        reply.setParent(parent);
        reply.setCreatedAt(LocalDateTime.now());

        PostComment savedReply = postCommentRepository.save(reply);
        notificationService.notify(currentUser.getId(), parent.getUser().getId(),
                Notification.NotificationType.REPLY_COMMENT, Notification.ReferenceType.COMMENT, savedReply.getId());
        // chỉ trả về reply mới, KHÔNG load lại parent
        return DisplayCommentDTO.mapToDTO(savedReply, currentUser, getMentionedUsersByCommentId(savedReply.getId()), friendshipService);
    }


}