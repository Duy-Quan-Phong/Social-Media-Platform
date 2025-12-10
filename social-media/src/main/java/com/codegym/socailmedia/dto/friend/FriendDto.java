package com.codegym.socailmedia.dto.friend;

import com.codegym.socailmedia.model.account.User;
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

    // --- THÊM DÒNG NÀY ĐỂ CHỨA LÝ DO GỢI Ý ---
    private String reason;

    // Constructor hỗ trợ convert từ User
    public FriendDto(User user, int mutualFriends) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.avatarUrl = user.getProfilePicture();
        this.fullName = user.getFirstName() + " " + user.getLastName();
        this.mutualFriends = mutualFriends;
        // Kiểm tra null để tránh lỗi nếu user chưa có privacy settings
        if (user.getPrivacySettings() != null) {
            this.allowFriendRequests = user.getPrivacySettings().isAllowFriendRequests();
        } else {
            this.allowFriendRequests = true; // Mặc định
        }
    }
}