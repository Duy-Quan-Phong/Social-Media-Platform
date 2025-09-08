package com.codegym.socialmedia.model.admin;
import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter; // null nếu là admin xử lý trực tiếp

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private Integer targetId;

    @Enumerated(EnumType.STRING)
    private ModerationActionType actionType;

    private String actionLevel;

    @ManyToOne
    @JoinColumn(name = "report_type_id")
    private ReportType reportType;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    public enum TargetType {
        USER, COMMENT, STATUS
    }

    public enum ModerationActionType {
        REPORT, WARNING, BAN, UNBAN, VERIFY, SUSPEND
    }


}
