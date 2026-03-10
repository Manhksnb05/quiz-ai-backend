package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class CustomExamSummaryDTO {
    private Long id;
    private String title;
    private Integer timeLimit;
    private Integer questionCount;
    private String createdAt;
}
