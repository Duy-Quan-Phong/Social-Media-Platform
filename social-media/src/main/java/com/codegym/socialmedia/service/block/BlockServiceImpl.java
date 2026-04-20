package com.codegym.socialmedia.service.block;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.BlockedUsers;
import com.codegym.socialmedia.repository.BlockedUsersRepository;
import com.codegym.socialmedia.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlockServiceImpl implements BlockService {

    @Autowired
    private BlockedUsersRepository blockedUsersRepository;

    @Autowired
    private IUserRepository userRepository;

    @Override
    public void blockUser(User blocker, User blocked) {
        if (blocker.getId().equals(blocked.getId())) return;
        if (!blockedUsersRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            BlockedUsers block = new BlockedUsers();
            block.setBlocker(blocker);
            block.setBlocked(blocked);
            blockedUsersRepository.save(block);
        }
    }

    @Override
    public void unblockUser(User blocker, User blocked) {
        blockedUsersRepository.findByBlockerAndBlocked(blocker, blocked)
                .ifPresent(blockedUsersRepository::delete);
    }

    @Override
    public boolean isBlocked(Long blockerId, Long blockedId) {
        User blocker = userRepository.findById(blockerId).orElse(null);
        User blocked = userRepository.findById(blockedId).orElse(null);
        if (blocker == null || blocked == null) return false;
        return blockedUsersRepository.existsByBlockerAndBlocked(blocker, blocked);
    }

    @Override
    public boolean isBlockedEither(Long userId1, Long userId2) {
        return isBlocked(userId1, userId2) || isBlocked(userId2, userId1);
    }
}
