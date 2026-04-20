package com.codegym.socialmedia.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PollDto {
    private Long id;
    private Long postId;
    private String question;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime endsAt;
    private int totalVotes;
    private Long myVotedOptionId;
    private List<OptionDto> options;

    @Data
    public static class OptionDto {
        private Long id;
        private String text;
        private int voteCount;
        private int percentage;
        private boolean votedByMe;

        public OptionDto(Long id, String text, int voteCount, int percentage, boolean votedByMe) {
            this.id = id;
            this.text = text;
            this.voteCount = voteCount;
            this.percentage = percentage;
            this.votedByMe = votedByMe;
        }
    }
}
