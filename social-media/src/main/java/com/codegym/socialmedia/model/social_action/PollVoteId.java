package com.codegym.socialmedia.model.social_action;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollVoteId implements Serializable {
    private Long userId;
    private Long optionId;
}
