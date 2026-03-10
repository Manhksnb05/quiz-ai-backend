package com.hutech.quizbackend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace(); // In ra log IntelliJ để Mạnh soi lỗi
        return ResponseEntity.internalServerError().body("Hệ thống lỗi: " + e.getMessage());
    }
}