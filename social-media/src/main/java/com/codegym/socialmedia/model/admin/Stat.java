package com.codegym.socialmedia.model.admin;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"admin_id", "stats_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statsId;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "stats_date")
    private LocalDate statsDate;

    private Integer totalUsers;
    private Integer activeUsers;
    private Integer newRegistrations;

    private Integer totalPosts;

    private Integer reportedContent;
    private Integer blockedUsers;

    private LocalDateTime generatedAt;

    @Column(columnDefinition = "JSON")
    private String detailedMetrics;

}
