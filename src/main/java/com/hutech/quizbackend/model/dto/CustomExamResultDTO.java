package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class CustomExamResultDTO {
    private Long resultId; // Trả về ID của record kết quả vừa lưu
    private int totalQuestions;
    private int correctAnswers;
    private double score;
}