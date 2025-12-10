package com.codegym.socailmedia.model.social_action;

import com.codegym.socailmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "blocked_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockedUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer blockId;

    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(columnDefinition = "TEXT")
    private String reason;

}
