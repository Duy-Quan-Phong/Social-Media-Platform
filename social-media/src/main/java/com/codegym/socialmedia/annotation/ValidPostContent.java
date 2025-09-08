package com.codegym.socialmedia.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostContentValidator.class)
@Documented
public @interface ValidPostContent {
    String message() default "Nội dung hoặc ảnh phải có ít nhất một";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
