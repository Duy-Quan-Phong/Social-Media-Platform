package com.codegym.socialmedia.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {
    String message() default "Giá trị đã tồn tại";
    Class<?> entityClass();  // Entity cần kiểm tra
    String fieldName();      // Tên trường trong entity
    String idField() default "id"; // Trường ID để loại trừ khi update
}