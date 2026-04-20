package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.PollOption;
import com.codegym.socialmedia.model.social_action.PollVote;
import com.codegym.socialmedia.model.social_action.PollVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, PollVoteId> {

    // Check if user already voted in this poll
    @Query("SELECT v FROM PollVote v WHERE v.user = :user AND v.option.poll.id = :pollId")
    Optional<PollVote> findByUserAndPollId(@Param("user") User user, @Param("pollId") Long pollId);

    // Count votes per option
    long countByOption(PollOption option);
}
