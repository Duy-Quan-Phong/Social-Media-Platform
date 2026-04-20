package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.PrivacyLevel;
import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 5000)
    private String content;

    @Column(columnDefinition = "JSON")
    private String imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrivacyLevel privacyCommentLevel = PrivacyLevel.PUBLIC;

    @Column(nullable = false)
    private boolean isDeleted = false;

    // Shared from another post (null = original)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_from_id")
    @ToString.Exclude
    private Post sharedFrom;

    @ManyToMany
    @JoinTable(
        name = "post_hashtags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @ToString.Exclude
    private Set<Hashtag> hashtags = new HashSet<>();

    @Column(name = "created_at", columnDefinition = "DATETIME")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<LikePost> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PostComment> comments;

    @Override
    public String toString() {
        return "[id: " + id + ", content: " + content + "]";
    }
}
