package com.codegym.socialmedia.annotation;

import com.codegym.socialmedia.dto.post.PostCreateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PostContentValidator implements ConstraintValidator<ValidPostContent, PostCreateDto> {

    @Override
    public boolean isValid(PostCreateDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        boolean hasContent = dto.getContent() != null && !dto.getContent().trim().isEmpty();

        boolean hasImages = dto.getImages() != null &&
                dto.getImages().stream().anyMatch(f -> f != null && !f.isEmpty());

        if (!hasContent && !hasImages) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Bài viết phải có nội dung hoặc ít nhất một ảnh")
                    .addPropertyNode("content")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}
