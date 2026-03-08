package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class PracticeFeedbackDTO {
    private Long questionId;
    private boolean isCorrect;
    // Cố tình không trả về 'correctAnswer' (đáp án đúng) để ép người dùng tự suy nghĩ và chọn lại
}