package com.codegym.socialmedia.dto.comment;

import com.codegym.socialmedia.component.PrivacyUtils;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.CommentMention;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.*;
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
    private List<MentionDTO> mentions;
    private Long parentCommentId;

    public DisplayCommentDTO(PostComment comment, boolean isLikedByCurrentUser,List<User> listUser) {
        this.username = comment.getUser().getUsername();
        this.userAvatarUrl = comment.getUser().getProfilePicture();
        this.userFullName = comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.comment = renderContent(comment.getContent(), listUser);
        this.commentId = comment.getId();
        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.parentCommentId = comment.getParent() != null ? comment.getParent().getId() : null;
        this.mentions = listUser.stream().map(u -> new MentionDTO(u.getId(), u.getUsername(), u.getFullName())).toList();
    }

    /**
     * Render nội dung comment với mention link.
     */
    private static String renderContent(String content, List<User> mentions) {
        if (content == null) return "";

        // 1) Escape toàn bộ nội dung comment
        String escaped = HtmlUtils.htmlEscape(content);

        // 2) Với mỗi user được mention, thay token @"Full Name" (đã escape) thành <a class="mention" ...>
        String rendered = escaped;
        for (User u : mentions) {
            String mentionName = u.getFirstName() + " " + u.getLastName();

            // escape mention token để khớp với escaped content
            String escapedMentionToken = HtmlUtils.htmlEscape("@" + mentionName);

            // build anchor với class "mention" và attribute data-username (tiện xử lý client)
            String anchor = "<a class=\"mention\" href=\"/profile/" + HtmlUtils.htmlEscape(u.getUsername())
                    + "\" data-username=\"" + HtmlUtils.htmlEscape(u.getUsername()) + "\""
                    + " aria-label=\"mention " + HtmlUtils.htmlEscape(mentionName) + "\">"
                    + HtmlUtils.htmlEscape("@" + mentionName) + "</a>";

            // Thay tất cả occurrences (an toàn vì cả hai đã escape)
            rendered = rendered.replace(escapedMentionToken, anchor);
        }

        return rendered;
    }

    /**
     * Convert một comment thành DTO với depth control (tối đa 3 cấp).
     */
    public static DisplayCommentDTO mapToDTO(PostComment comment,
                                             User currentUser, List<User> mentionedUsers,
                                             FriendshipService friendshipService) {
        return getComment(comment, currentUser, mentionedUsers,friendshipService, 3);
    }

    private static DisplayCommentDTO getComment(PostComment comment,
                                                User currentUser, List<User> mentionedUsers,
                                                FriendshipService friendshipService,
                                                int depth) {
        // Build base DTO
        DisplayCommentDTO dto = buildDtoBase(comment, currentUser,  mentionedUsers, friendshipService);

        // Handle replies theo depth
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            if (depth > 1) {
                List<DisplayCommentDTO> replies = new ArrayList<>();
                for (PostComment reply : sortComments(comment.getReplies())) {
                    replies.add(getComment(reply, currentUser,mentionedUsers, friendshipService, depth - 1));
                }
                dto.setReplies(replies);
            } else {
                List<DisplayCommentDTO> flatDescendants = new ArrayList<>();
                collectDescendantsFlat(comment, flatDescendants, currentUser, mentionedUsers,friendshipService);
                dto.setReplies(flatDescendants.isEmpty() ? null : flatDescendants);
            }
        } else {
            dto.setReplies(null);
        }
        return dto;
    }

    /**
     * Build dữ liệu cơ bản cho DTO (không set replies).
     */
    private static DisplayCommentDTO buildDtoBase(PostComment comment,
                                                  User currentUser, List<User> mentionedUsers,
                                                  FriendshipService friendshipService) {
        DisplayCommentDTO dto = new DisplayCommentDTO();
        Post p = comment.getPost();

        // Quyền reply
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

        // Base info
        dto.setCommentId(comment.getId());
        dto.setComment(renderContent(comment.getContent(), mentionedUsers));
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setUserFullName(comment.getUser().getFullName());
        dto.setUsername(comment.getUser().getUsername());
        dto.setUserAvatarUrl(comment.getUser().getProfilePicture());

        dto.setCanEdit(currentUser != null && comment.getUser().getId().equals(currentUser.getId()));
        dto.setCanDeleted(currentUser != null && comment.getUser().getId().equals(currentUser.getId()));
        dto.setParentCommentId(comment.getParent() != null ? comment.getParent().getId() : null);

        // Like
        int likeCount = comment.getLikedByUsers() != null ? comment.getLikedByUsers().size() : 0;
        dto.setLikeCount(likeCount);

        boolean likedByCurrentUser = currentUser != null && comment.getLikedByUsers() != null &&
                comment.getLikedByUsers().stream()
                        .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
        dto.setLikedByCurrentUser(likedByCurrentUser);

        // Mentions
        dto.setMentions(mentionedUsers.stream()
                .map(u -> new MentionDTO(u.getId(), u.getUsername(), u.getFullName()))
                .toList());

        return dto;
    }


    private static void collectDescendantsFlat(PostComment root,
                                               List<DisplayCommentDTO> collector,
                                               User currentUser, List<User> mentionedUsers,
                                               FriendshipService friendshipService) {
        if (root.getReplies() == null || root.getReplies().isEmpty()) return;

        for (PostComment child : sortComments(root.getReplies())) {
            DisplayCommentDTO leaf = buildDtoBase(child, currentUser,mentionedUsers, friendshipService);
            leaf.setReplies(null);
            collector.add(leaf);
            collectDescendantsFlat(child, collector, currentUser, mentionedUsers,friendshipService);
        }
    }

    private static List<PostComment> sortComments(Collection<PostComment> comments) {
        return comments.stream()
                .sorted(Comparator.comparing(PostComment::getCreatedAt).reversed())
                .toList();
    }
}
