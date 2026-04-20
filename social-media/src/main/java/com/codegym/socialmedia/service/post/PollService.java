package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.PollDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.*;
import com.codegym.socialmedia.repository.PollRepository;
import com.codegym.socialmedia.repository.PollVoteRepository;
import com.codegym.socialmedia.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PollService {

    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final PostRepository postRepository;

    public PostPoll createPoll(Long postId, String question, List<String> optionTexts, java.time.LocalDateTime endsAt) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostPoll poll = new PostPoll();
        poll.setPost(post);
        poll.setQuestion(question);
        poll.setEndsAt(endsAt);

        for (String text : optionTexts) {
            PollOption option = new PollOption();
            option.setPoll(poll);
            option.setText(text.trim());
            poll.getOptions().add(option);
        }
        return pollRepository.save(poll);
    }

    public PollDto vote(Long pollId, Long optionId, User user) {
        PostPoll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        if (poll.getEndsAt() != null && poll.getEndsAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Poll has ended");
        }

        // Remove previous vote in this poll if any
        pollVoteRepository.findByUserAndPollId(user, pollId)
                .ifPresent(pollVoteRepository::delete);

        PollOption option = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Option not found"));

        PollVote vote = new PollVote();
        vote.setId(new PollVoteId(user.getId(), optionId));
        vote.setUser(user);
        vote.setOption(option);
        pollVoteRepository.save(vote);

        return toPollDto(poll, user);
    }

    @Transactional(readOnly = true)
    public PollDto getPollDto(Long postId, User currentUser) {
        return pollRepository.findByPostId(postId)
                .map(poll -> toPollDto(poll, currentUser))
                .orElse(null);
    }

    private PollDto toPollDto(PostPoll poll, User currentUser) {
        PollDto dto = new PollDto();
        dto.setId(poll.getId());
        dto.setQuestion(poll.getQuestion());
        dto.setEndsAt(poll.getEndsAt());
        dto.setPostId(poll.getPost().getId());

        Long myVotedOptionId = currentUser != null
                ? pollVoteRepository.findByUserAndPollId(currentUser, poll.getId())
                        .map(v -> v.getOption().getId()).orElse(null)
                : null;

        int totalVotes = poll.getOptions().stream().mapToInt(o -> (int) pollVoteRepository.countByOption(o)).sum();
        dto.setTotalVotes(totalVotes);

        List<PollDto.OptionDto> options = poll.getOptions().stream().map(o -> {
            int count = (int) pollVoteRepository.countByOption(o);
            return new PollDto.OptionDto(o.getId(), o.getText(), count,
                    totalVotes > 0 ? (count * 100 / totalVotes) : 0,
                    o.getId().equals(myVotedOptionId));
        }).toList();
        dto.setOptions(options);
        dto.setMyVotedOptionId(myVotedOptionId);
        return dto;
    }
}
