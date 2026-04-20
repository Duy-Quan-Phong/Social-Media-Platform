package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.annotation.UniqueValidator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalHandlerController {

    @Autowired
    private UniqueValidator validator;

    @InitBinder
    public void initBinder(org.springframework.web.bind.WebDataBinder binder) {
        binder.addValidators(validator);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "Không tìm thấy dữ liệu"
        ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleNoSuchElement(NoSuchElementException e) {
        return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "Không tìm thấy dữ liệu"
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Bạn không có quyền thực hiện hành động này"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "Dữ liệu không hợp lệ"
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra, vui lòng thử lại"
        ));
    }
}
