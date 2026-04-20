package com.codegym.socialmedia.service.block;

import com.codegym.socialmedia.model.account.User;

public interface BlockService {
    void blockUser(User blocker, User blocked);
    void unblockUser(User blocker, User blocked);
    boolean isBlocked(Long blockerId, Long blockedId);
    boolean isBlockedEither(Long userId1, Long userId2);
}
