package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.UserBlock;
import com.codegym.socialmedia.model.social_action.UserBlockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UserBlockId> {

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    void deleteByBlockerAndBlocked(User blocker, User blocked);

    @Query("SELECT ub.blocked.id FROM UserBlock ub WHERE ub.blocker.id = :userId")
    List<Long> findBlockedUserIds(@Param("userId") Long userId);

    @Query("SELECT ub.blocker.id FROM UserBlock ub WHERE ub.blocked.id = :userId")
    List<Long> findBlockerUserIds(@Param("userId") Long userId);
}
