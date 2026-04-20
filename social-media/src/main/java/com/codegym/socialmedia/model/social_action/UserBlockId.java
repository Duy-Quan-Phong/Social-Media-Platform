package com.codegym.socialmedia.model.social_action;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBlockId implements Serializable {
    private Long blockerId;
    private Long blockedId;
}
