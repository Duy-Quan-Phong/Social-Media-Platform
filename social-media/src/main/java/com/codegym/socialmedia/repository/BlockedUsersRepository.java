package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.BlockedUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface BlockedUsersRepository extends JpaRepository<BlockedUsers, Integer> {
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    Optional<BlockedUsers> findByBlockerAndBlocked(User blocker, User blocked);
    List<BlockedUsers> findByBlocker(User blocker);
    List<BlockedUsers> findByBlocked(User blocked);
}
