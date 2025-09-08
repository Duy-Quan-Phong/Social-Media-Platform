package com.codegym.socialmedia.annotation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.lang.reflect.Field;
import java.util.Objects;

@Component
public class UniqueValidator implements Validator {

    @Autowired
    private EntityManager entityManager;

    @Override
    public boolean supports(Class<?> clazz) {
        return true; // Hỗ trợ validate mọi class
    }

    @Override
    public void validate(Object target, Errors errors) {
        Class<?> clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Unique.class)) {
                validateField(target, field, errors);
            }
        }
    }

//    private void validateField(Object target, Field field, Errors errors) {
//        try {
//            Unique annotation = field.getAnnotation(Unique.class);
//            field.setAccessible(true);
//            Object value = field.get(target);
//
//            if (value == null) {
//                return; // Bỏ qua nếu giá trị null
//            }
//
//            // Lấy ID của entity hiện tại (nếu có)
//            Object currentId = getCurrentId(target, annotation.idField());
//
//            // Kiểm tra tính duy nhất
//            boolean isDuplicate = checkDuplicateValue(
//                    annotation.entityClass(),
//                    annotation.fieldName(),
//                    value,
//                    currentId,
//                    annotation.idField()
//            );
//
//            if (isDuplicate) {
//                errors.rejectValue(
//                        field.getName(),
//                        "unique",
//                        annotation.message()
//                );
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("Validation error", e);
//        }
//    }
private void validateField(Object target, Field field, Errors errors) {
    try {
        Unique annotation = field.getAnnotation(Unique.class);
        field.setAccessible(true);
        Object newValue = field.get(target);

        if (newValue == null) {
            return; // Bỏ qua nếu giá trị null
        }

        // Lấy ID của bản ghi hiện tại
        Object currentId = getCurrentId(target, annotation.idField());

        // Nếu có ID thì truy vấn entity từ DB để lấy giá trị cũ
        if (currentId != null) {
            Object existingEntity = entityManager.find(annotation.entityClass(), currentId);
            if (existingEntity != null) {
                Field entityField = annotation.entityClass().getDeclaredField(annotation.fieldName());
                entityField.setAccessible(true);
                Object existingValue = entityField.get(existingEntity);

                // Nếu giá trị không thay đổi thì bỏ qua kiểm tra
                if (Objects.equals(existingValue, newValue)) {
                    return;
                }
            }
        }

        // Thực hiện kiểm tra trùng
        boolean isDuplicate = checkDuplicateValue(
                annotation.entityClass(),
                annotation.fieldName(),
                newValue,
                currentId,
                annotation.idField()
        );

        if (isDuplicate) {
            errors.rejectValue(
                    field.getName(),
                    "unique",
                    annotation.message()
            );
        }

    } catch (Exception e) {
        throw new RuntimeException("Validation error", e);
    }
}

    private Object getCurrentId(Object target, String idFieldName) throws Exception {
        try {
            Field idField = target.getClass().getDeclaredField(idFieldName);
            idField.setAccessible(true);
            return idField.get(target);
        } catch (NoSuchFieldException e) {
            return null; // Không có ID (trường hợp tạo mới)
        }
    }

    private boolean checkDuplicateValue(
            Class<?> entityClass,
            String fieldName,
            Object value,
            Object currentId,
            String idField
    ) {
        String queryStr = String.format(
                "SELECT COUNT(e) FROM %s e WHERE e.%s = :value",
                entityClass.getSimpleName(),
                fieldName
        );

        if (currentId != null) {
            queryStr += String.format(" AND e.%s != :currentId", idField);
        }

        Query query = entityManager.createQuery(queryStr)
                .setParameter("value", value);

        if (currentId != null) {
            query.setParameter("currentId", currentId);
        }

        Long count = (Long) query.getSingleResult();
        return count > 0;
    }
}