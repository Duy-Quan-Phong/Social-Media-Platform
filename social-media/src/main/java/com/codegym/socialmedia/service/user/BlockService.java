package com.codegym.socialmedia.service.user;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.UserBlock;
import com.codegym.socialmedia.model.social_action.UserBlockId;
import com.codegym.socialmedia.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserService userService;

    public boolean block(Long targetUserId) {
        User current = userService.getCurrentUser();
        User target = userService.getUserById(targetUserId);
        if (current == null || target == null || current.getId().equals(targetUserId)) return false;

        if (userBlockRepository.existsByBlockerAndBlocked(current, target)) return false;

        UserBlock block = new UserBlock();
        block.setId(new UserBlockId(current.getId(), targetUserId));
        block.setBlocker(current);
        block.setBlocked(target);
        userBlockRepository.save(block);
        return true;
    }

    public boolean unblock(Long targetUserId) {
        User current = userService.getCurrentUser();
        User target = userService.getUserById(targetUserId);
        if (current == null || target == null) return false;

        if (!userBlockRepository.existsByBlockerAndBlocked(current, target)) return false;
        userBlockRepository.deleteByBlockerAndBlocked(current, target);
        return true;
    }

    public boolean isBlocked(Long blockerId, Long blockedId) {
        User blocker = userService.getUserById(blockerId);
        User blocked = userService.getUserById(blockedId);
        if (blocker == null || blocked == null) return false;
        return userBlockRepository.existsByBlockerAndBlocked(blocker, blocked);
    }

    public boolean isBlockedEitherWay(Long userAId, Long userBId) {
        return isBlocked(userAId, userBId) || isBlocked(userBId, userAId);
    }

    @Transactional(readOnly = true)
    public List<Long> getBlockedUserIds(Long userId) {
        return userBlockRepository.findBlockedUserIds(userId);
    }
}
