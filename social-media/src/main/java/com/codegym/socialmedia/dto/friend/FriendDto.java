package com.codegym.socialmedia.dto.friend;

import com.codegym.socialmedia.model.account.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendDto {
    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;
    private int mutualFriends;
    private boolean allowFriendRequests;
    public FriendDto(User user, int mutualFriends) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.avatarUrl = user.getProfilePicture();
        this.fullName = user.getFirstName() + " " + user.getLastName();
        this.mutualFriends = mutualFriends;
        this.allowFriendRequests = user.getPrivacySettings().isAllowFriendRequests();
    }
}
