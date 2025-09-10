package com.codegym.socialmedia.dto.comment;

import com.codegym.socialmedia.component.PrivacyUtils;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisplayCommentDTO {

    // User info
    private String username;
    private String userFullName;
    private String userAvatarUrl;

    // Comment info
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime updatedAt;
    private String comment;
    private Long commentId;
    private int likeCount;
    private boolean isLikedByCurrentUser;
    private boolean canDeleted;
    private boolean canEdit;
    private boolean canReply;
    private List<DisplayCommentDTO> replies;
    private Long parentCommentId;

    public DisplayCommentDTO(PostComment comment, boolean isLikedByCurrentUser) {
        this.username = comment.getUser().getUsername();
        this.userAvatarUrl = comment.getUser().getProfilePicture();
        this.userFullName = comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.comment = comment.getContent();
        this.commentId = comment.getId();
        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.parentCommentId = comment.getParent() !=null ? comment.getParent().getId() : null;
    }

    private static List<PostComment> sortComments(Collection<PostComment> comments) {
        return comments.stream()
                .sorted(Comparator.comparing(PostComment::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public static DisplayCommentDTO mapToDTO(PostComment comment,
                                             User currentUser,
                                             FriendshipService friendshipService) {
        // depth = 3 => giữ: root (level0) -> child (level1) -> grandchild (level2)
        // và bắt đầu flatten từ level3 (chắt) trở xuống
        return getComment(comment, currentUser, friendshipService, 3);
    }

    private static DisplayCommentDTO getComment(PostComment comment,
                                                User currentUser,
                                                FriendshipService friendshipService,
                                                int depth) {
        // Tạo DTO cơ bản (KHÔNG set replies ở đây)
        DisplayCommentDTO dto = buildDtoBase(comment, currentUser, friendshipService);

        // Xử lý replies tùy depth
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            if (depth > 1) {
                // Giữ nested: đệ quy cho mỗi reply với depth-1
                List<DisplayCommentDTO> replies = new ArrayList<>();
                for (PostComment reply : sortComments(comment.getReplies())) {
                    replies.add(getComment(reply, currentUser, friendshipService, depth - 1));
                }
                dto.setReplies(replies);
            } else {
                // depth == 1: đây là cấp "cháu" theo nghĩa ta muốn bắt đầu flatten từ chắt
                // => gom phẳng tất cả hậu duệ (con trực tiếp, chắt, chít, ...) vào danh sách replies của DTO này
                List<DisplayCommentDTO> flatDescendants = new ArrayList<>();
                collectDescendantsFlat(comment, flatDescendants, currentUser, friendshipService);
                dto.setReplies(flatDescendants.isEmpty() ? null : flatDescendants);
            }
        } else {
            // không có replies => giữ null (đúng với JSON mẫu của bạn)
            dto.setReplies(null);
        }

        return dto;
    }

    /**
     * Tạo DTO cơ bản cho 1 comment (KHÔNG set nested replies).
     */
    private static DisplayCommentDTO buildDtoBase(PostComment comment,
                                                  User currentUser,
                                                  FriendshipService friendshipService) {
        DisplayCommentDTO dto = new DisplayCommentDTO();
        Post p = comment.getPost();

        boolean canReply;
        if (currentUser != null && currentUser.isAdmin()) {
            canReply = true;
        } else {
            boolean isFriend = false;
            if (currentUser != null) {
                Friendship.FriendshipStatus friendshipStatus =
                        friendshipService.getFriendshipStatus(p.getUser(), currentUser);
                isFriend = (friendshipStatus == Friendship.FriendshipStatus.ACCEPTED);
            }
            canReply = PrivacyUtils.canView(currentUser, p.getUser(), p.getPrivacyCommentLevel(), isFriend);
        }
        dto.setCanReply(canReply);

        dto.setCommentId(comment.getId());
        dto.setComment(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setUserFullName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        dto.setUsername(comment.getUser().getUsername());
        dto.setUserAvatarUrl(comment.getUser().getProfilePicture());

        dto.setCanEdit(currentUser != null && comment.getUser().getId().equals(currentUser.getId()));
        dto.setCanDeleted(currentUser != null && comment.getUser().getId().equals(currentUser.getId()));
        dto.setParentCommentId(comment.getParent() != null ? comment.getParent().getId() : null);

        int likeCount = comment.getLikedByUsers() != null ? comment.getLikedByUsers().size() : 0;
        dto.setLikeCount(likeCount);

        boolean likedByCurrentUser = currentUser != null && comment.getLikedByUsers() != null &&
                comment.getLikedByUsers().stream().anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
        dto.setLikedByCurrentUser(likedByCurrentUser);

        // DON'T set dto.setReplies(...) ở đây — caller sẽ quyết định nested hoặc flat
        return dto;
    }

    /**
     * Gom phẳng tất cả hậu duệ (không bao gồm chính 'root').
     * Mỗi phần tử collector là 1 DTO leaf (không có nested replies).
     */
    private static void collectDescendantsFlat(PostComment root,
                                               List<DisplayCommentDTO> collector,
                                               User currentUser,
                                               FriendshipService friendshipService) {
        if (root.getReplies() == null || root.getReplies().isEmpty()) return;

        for (PostComment child : sortComments(root.getReplies())) {
            // Tạo DTO leaf cho child (KHÔNG set nested replies)
            DisplayCommentDTO leaf = buildDtoBase(child, currentUser, friendshipService);
            leaf.setReplies(null);
            collector.add(leaf);

            // Đệ quy xuống các thế hệ tiếp theo
            collectDescendantsFlat(child, collector, currentUser, friendshipService);
        }
    }

}