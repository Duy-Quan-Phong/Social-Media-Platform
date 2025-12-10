// PostUpdateDto.java
package com.codegym.socailmedia.dto.post;

import com.codegym.socailmedia.model.PrivacyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDto {

    private Long id;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    private PrivacyLevel privacyLevel;
    private PrivacyLevel commentPrivacyLevel;
    private List<MultipartFile> newImages;

    private List<String> existingImages;

    private List<String> imagesToDelete;
}