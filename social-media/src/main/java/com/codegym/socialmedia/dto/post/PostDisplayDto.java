// PostDisplayDto.java
package com.codegym.socialmedia.dto.post;

import com.codegym.socialmedia.dto.PollDto;
import com.codegym.socialmedia.model.PrivacyLevel;
import com.codegym.socialmedia.model.social_action.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDisplayDto {

    private Long id;
    private String content;
    private List<String> imageUrls;
    private PrivacyLevel privacyLevel;
    private PrivacyLevel privacyCommentLevel;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime updatedAt;

    // User info
    private Long userId;
    private String username;
    private String userFullName;
    private String userAvatarUrl;

    // Stats
    private int likesCount;
    private int commentsCount;
    private boolean isLikedByCurrentUser;
    private boolean isSavedByCurrentUser;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canComment;

    // Reactions
    private Map<String, Integer> reactionCounts;
    private String currentUserReaction;

    // Hashtags
    private Set<String> hashtags;

    // Shared post info (null if original post)
    private Long sharedFromId;
    private String sharedFromUsername;
    private String sharedFromUserFullName;
    private String sharedFromUserAvatarUrl;
    private String sharedFromContent;
    private List<String> sharedFromImageUrls;

    // Poll (null if no poll)
    private PollDto poll;

    public PostDisplayDto(Post post, boolean isLikedByCurrentUser, boolean canEdit, boolean canDelete) {
        this.id = post.getId();
        this.content = post.getContent();
        this.imageUrls = parseImageUrls(post.getImageUrls());
        this.privacyLevel = post.getPrivacyLevel();
        this.privacyCommentLevel = post.getPrivacyCommentLevel();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        this.userId = post.getUser().getId();
        this.username = post.getUser().getUsername();
        this.userFullName = post.getUser().getFirstName() + " " + post.getUser().getLastName();
        this.userAvatarUrl = post.getUser().getProfilePicture();

        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.canEdit = canEdit;
        this.canDelete = canDelete;

        if (post.getHashtags() != null) {
            this.hashtags = post.getHashtags().stream()
                    .map(h -> h.getName())
                    .collect(Collectors.toSet());
        }

        if (post.getSharedFrom() != null) {
            Post src = post.getSharedFrom();
            this.sharedFromId = src.getId();
            this.sharedFromUsername = src.getUser().getUsername();
            this.sharedFromUserFullName = src.getUser().getFirstName() + " " + src.getUser().getLastName();
            this.sharedFromUserAvatarUrl = src.getUser().getProfilePicture();
            this.sharedFromContent = src.getContent();
            this.sharedFromImageUrls = parseImageUrls(src.getImageUrls());
        }
    }

    private List<String> parseImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.trim().isEmpty()) {
            return List.of();
        }
        // Parse JSON array string to List<String>
        try {
            // Simple JSON parsing - in production, use Jackson
            String cleaned = imageUrlsJson.replaceAll("[\\[\\]\"]", "");
            if (cleaned.trim().isEmpty()) {
                return List.of();
            }
            return List.of(cleaned.split(","));
        } catch (Exception e) {
            return List.of();
        }
    }
}