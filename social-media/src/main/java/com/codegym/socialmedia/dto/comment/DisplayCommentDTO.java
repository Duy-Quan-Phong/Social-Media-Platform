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
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.*;

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

    /**
     * Convert một comment thành DTO với depth control (tối đa 3 cấp).
     */
    public static DisplayCommentDTO mapToDTO(PostComment comment,
                                             User currentUser,
                                             FriendshipService friendshipService) {
        return getComment(comment, currentUser, friendshipService, 3);
    }

    private static DisplayCommentDTO getComment(PostComment comment,
                                                User currentUser,
                                                FriendshipService friendshipService,
                                                int depth) {
        // Build base DTO
        DisplayCommentDTO dto = buildDtoBase(comment, currentUser, friendshipService);

        // Render content sau khi build base
        dto.setComment(renderContent(comment.getContent(), comment.getMentionedUsers()));

        // Xử lý replies
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            if (depth > 1) {
                List<DisplayCommentDTO> replies = new ArrayList<>();
                for (PostComment reply : sortComments(comment.getReplies())) {
                    DisplayCommentDTO replyDTO =
                            getComment(reply, currentUser, friendshipService, depth - 1);
                    replies.add(replyDTO);
                }
                dto.setReplies(replies);
            } else {
                List<DisplayCommentDTO> flatDescendants = new ArrayList<>();
                collectDescendantsFlat(comment, flatDescendants, currentUser, friendshipService);
                dto.setReplies(flatDescendants.isEmpty() ? null : flatDescendants);
            }
        } else {
            dto.setReplies(null);
        }

        return dto;
    }

    /**
     * Build dữ liệu cơ bản cho DTO (không set replies, không render content).
     */
    private static DisplayCommentDTO buildDtoBase(PostComment comment,
                                                  User currentUser,
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
        List<User> mentions = comment.getMentionedUsers();
        dto.setMentions(mentions.stream()
                .map(u -> new MentionDTO(u.getId(), u.getUsername(), u.getFullName()))
                .toList());

        return dto;
    }

    private static void collectDescendantsFlat(PostComment root,
                                               List<DisplayCommentDTO> collector,
                                               User currentUser,
                                               FriendshipService friendshipService) {
        if (root.getReplies() == null || root.getReplies().isEmpty()) return;

        for (PostComment child : sortComments(root.getReplies())) {
            DisplayCommentDTO leaf = buildDtoBase(child, currentUser, friendshipService);
            leaf.setComment(renderContent(child.getContent(), child.getMentionedUsers()));
            leaf.setReplies(null);
            collector.add(leaf);
            collectDescendantsFlat(child, collector, currentUser, friendshipService);
        }
    }

    private static List<PostComment> sortComments(Collection<PostComment> comments) {
        return comments.stream()
                .sorted(Comparator.comparing(PostComment::getCreatedAt).reversed())
                .toList();
    }

    /**
     * Render nội dung comment với mentions.
     */
    private static String renderContent(String content, List<User> mentions) {
        if (content == null || content.isEmpty()) return "";

        StringBuilder out = new StringBuilder();
        int idx = 0;
        int len = content.length();

        // Sắp xếp mentions theo tên dài trước
        List<User> sortedMentions = new ArrayList<>(mentions != null ? mentions : Collections.emptyList());
        sortedMentions.sort(Comparator.comparingInt(
                u -> -((u.getFirstName() == null ? 0 : u.getFirstName().length())
                        + (u.getLastName() == null ? 0 : u.getLastName().length()) + 1))
        );

        while (idx < len) {
            int at = content.indexOf('@', idx);
            if (at == -1) {
                out.append(HtmlUtils.htmlEscape(content.substring(idx)));
                break;
            }
            if (at > idx) {
                out.append(HtmlUtils.htmlEscape(content.substring(idx, at)));
            }

            boolean okBoundaryBefore = (at == 0) || Character.isWhitespace(content.charAt(at - 1));
            if (!okBoundaryBefore) {
                out.append(HtmlUtils.htmlEscape("@"));
                idx = at + 1;
                continue;
            }

            boolean matched = false;
            for (User u : sortedMentions) {
                String first = u.getFirstName() == null ? "" : u.getFirstName().trim();
                String last = u.getLastName() == null ? "" : u.getLastName().trim();
                if (first.isEmpty() && last.isEmpty()) continue;

                String fullName = (first + (last.isEmpty() ? "" : " " + last)).trim();
                if (fullName.isEmpty()) continue;

                int nameLen = fullName.length();
                int endPos = at + 1 + nameLen;

                if (endPos <= len) {
                    String candidate = content.substring(at + 1, endPos);
                    if (candidate.equalsIgnoreCase(fullName)) {
                        if (endPos == len || !Character.isLetterOrDigit(content.charAt(endPos))) {
                            String anchor = "<a class=\"mention\""
                                    + " href=\"/profile/" + HtmlUtils.htmlEscape(u.getUsername()) + "\""
                                    + " data-username=\"" + HtmlUtils.htmlEscape(u.getUsername()) + "\""
                                    + " aria-label=\"mention " + HtmlUtils.htmlEscape(fullName) + "\">"
                                    + HtmlUtils.htmlEscape("@" + fullName)
                                    + "</a>";
                            out.append(anchor);
                            idx = endPos;
                            matched = true;
                            break;
                        }
                    }
                }
            }

            if (!matched) {
                out.append(HtmlUtils.htmlEscape("@"));
                idx = at + 1;
            }
        }

        return out.toString();
    }
}
