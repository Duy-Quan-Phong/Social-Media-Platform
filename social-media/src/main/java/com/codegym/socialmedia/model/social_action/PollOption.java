package com.codegym.socialmedia.model.social_action;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "poll_id", nullable = false)
    @ToString.Exclude
    private PostPoll poll;

    @Column(nullable = false, length = 200)
    private String text;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PollVote> votes = new ArrayList<>();

    public int getVoteCount() {
        return votes.size();
    }
}
