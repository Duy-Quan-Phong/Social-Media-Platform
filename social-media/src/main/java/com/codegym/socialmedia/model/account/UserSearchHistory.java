package com.codegym.socialmedia.model.account;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "user_search_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String searchQuery;

    @Enumerated(EnumType.STRING)
    private SearchType searchType;

    @ManyToOne
    @JoinColumn(name = "result_user_id")
    private User resultUser;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum SearchType {
        USER, STATUS, GENERAL
    }
}
