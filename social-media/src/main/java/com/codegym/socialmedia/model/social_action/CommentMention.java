package com.codegym.socialmedia.model.social_action;


import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_mentions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentMention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "comment_id")
    private PostComment comment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mentioned_user_id")
    private User mentionedUser;
}
