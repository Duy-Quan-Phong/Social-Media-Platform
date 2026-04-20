package com.codegym.socialmedia.dto;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Story;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class StoryDto {
    private Long userId;
    private String username;
    private String userFullName;
    private String userAvatarUrl;
    private List<StoryItemDto> stories = new ArrayList<>();

    public StoryDto(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.userFullName = user.getFirstName() + " " + user.getLastName();
        this.userAvatarUrl = user.getProfilePicture();
    }

    public void addStory(Story s) {
        stories.add(new StoryItemDto(s));
    }

    @Data
    public static class StoryItemDto {
        private Long id;
        private String mediaUrl;
        private String mediaType;
        private String caption;
        private LocalDateTime expiresAt;
        private int viewCount;

        public StoryItemDto(Story s) {
            this.id = s.getId();
            this.mediaUrl = s.getMediaUrl();
            this.mediaType = s.getMediaType().name();
            this.caption = s.getCaption();
            this.expiresAt = s.getExpiresAt();
            this.viewCount = s.getViewCount();
        }
    }
}
