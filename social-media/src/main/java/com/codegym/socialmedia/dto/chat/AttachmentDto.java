package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDto {
    private String attachmentUrl;
    private String fileName;
    private Long fileSize;
    private String type; // IMAGE, VIDEO, FILE, AUDIO...
}

