package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class ExamPublicDTO {
    private Long id;
    private String title;
    private Integer totalQuestions;
    private String creatorName; // Tên người đã public bộ đề này
    private String createdAt;   // Ngày giờ tạo
}