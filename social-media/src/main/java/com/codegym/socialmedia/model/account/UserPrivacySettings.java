package com.codegym.socialmedia.model.account;

import com.codegym.socialmedia.model.PrivacyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects; // Import thêm Objects

@Entity
@Table(name = "user_privacy_settings")
@Getter // Thay cho @Data
@Setter // Thay cho @Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivacySettings {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Quyền xem các thông tin cá nhân
    @Enumerated(EnumType.STRING)
    private PrivacyLevel showProfile = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showFriendList = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showFullName = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showAddress = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showPhone = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showEmail = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showAvatar = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showBio = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showDob = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel allowSendMessage = PrivacyLevel.FRIENDS;

    // Tùy chọn tìm kiếm
    private boolean allowSearchByEmail = true;
    private boolean allowSearchByPhone = true;
    private boolean canBeFound = true;
    // Tùy chọn kết bạn
    private boolean allowFriendRequests = true;

    // --- PHẦN SỬA LỖI STACKOVERFLOW & BEST PRACTICE ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPrivacySettings)) return false;
        UserPrivacySettings that = (UserPrivacySettings) o;
        // Chỉ so sánh ID
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        // Trả về hash code cố định của class
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        // Chỉ in các thông tin settings, KHÔNG in object User để tránh vòng lặp
        return "UserPrivacySettings{" +
                "id=" + id +
                ", showProfile=" + showProfile +
                ", showFriendList=" + showFriendList +
                ", allowSearchByEmail=" + allowSearchByEmail +
                ", allowFriendRequests=" + allowFriendRequests +
                '}';
    }
}