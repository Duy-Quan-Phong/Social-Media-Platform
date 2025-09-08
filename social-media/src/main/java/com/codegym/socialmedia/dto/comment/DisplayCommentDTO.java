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
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisplayCommentDTO {

    private String id;

    // User info
    private Long userId;
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
        this.userId = comment.getUser().getId();
        this.username = comment.getUser().getUsername();
        this.userAvatarUrl = comment.getUser().getProfilePicture();
        this.userFullName = comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.comment = comment.getContent();
        this.commentId = comment.getId();
        this.id=comment.getId().toString();
        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.parentCommentId = comment.getParent() !=null ? comment.getParent().getId() : null;
    }

    public static DisplayCommentDTO mapToDTO(PostComment comment,
                                             User currentUser,
                                             FriendshipService friendshipService) {
        return mapToDTO(comment, currentUser, friendshipService, true);
    }

    private static DisplayCommentDTO mapToDTO(PostComment comment,
                                              User currentUser,
                                              FriendshipService friendshipService,
                                              boolean includeReplies) {
        DisplayCommentDTO dto = new DisplayCommentDTO();
        Post p = comment.getPost();

        Friendship.FriendshipStatus friendshipStatus =
                friendshipService.getFriendshipStatus(p.getUser(), currentUser);
        boolean isFriend = (friendshipStatus == Friendship.FriendshipStatus.ACCEPTED);

        dto.setCanReply(PrivacyUtils.canView(currentUser, p.getUser(), p.getPrivacyCommentLevel(), isFriend));

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

        // Like info
        int likeCount = comment.getLikedByUsers() != null ? comment.getLikedByUsers().size() : 0;
        dto.setLikeCount(likeCount);

        boolean likedByCurrentUser = currentUser != null && comment.getLikedByUsers() != null &&
                comment.getLikedByUsers().stream().anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
        dto.setLikedByCurrentUser(likedByCurrentUser);

        // Replies: flatten toàn bộ descendants (con, cháu, chắt, ...)
        if (includeReplies && comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<DisplayCommentDTO> flatReplies = new ArrayList<>();
            for (PostComment reply : comment.getReplies()) {
                collectAllReplies(reply, flatReplies, currentUser, friendshipService);
            }
            dto.setReplies(flatReplies);
        }

        return dto;
    }

    /**
     * Thu thập tất cả con cháu, flatten thành 1 cấp.
     */
    private static void collectAllReplies(PostComment comment,
                                          List<DisplayCommentDTO> collector,
                                          User currentUser,
                                          FriendshipService friendshipService) {
        // map bản thân reply (không lấy replies của nó ở đây)
        DisplayCommentDTO replyDto = mapToDTO(comment, currentUser, friendshipService, false);
        collector.add(replyDto);

        // đệ quy để lấy tiếp con cháu
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (PostComment child : comment.getReplies()) {
                collectAllReplies(child, collector, currentUser, friendshipService);
            }
        }
    }

}
