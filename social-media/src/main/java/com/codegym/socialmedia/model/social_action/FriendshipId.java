package com.codegym.socialmedia.model.social_action;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FriendshipId implements Serializable {
    private Long requesterId;
    private Long addresseeId;

}