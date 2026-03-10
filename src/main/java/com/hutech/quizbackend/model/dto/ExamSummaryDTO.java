package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class ExamSummaryDTO { // Dùng để hiển thị danh sách ở Dashboard
    private Long id;
    private String title;
    private Integer totalQuestions;
    private String status; // Private / Public
    private String createdAt;
}