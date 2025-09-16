package com.codegym.socialmedia.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentionDTO {
    private Long id;
    private String username;
    private String fullName;
}
