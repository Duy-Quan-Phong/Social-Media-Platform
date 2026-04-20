package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.PollOption;
import com.codegym.socialmedia.model.social_action.PollVote;
import com.codegym.socialmedia.model.social_action.PollVoteId;
import com.codegym.socialmedia.model.social_action.PostPoll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<PostPoll, Long> {
    Optional<PostPoll> findByPostId(Long postId);
}
