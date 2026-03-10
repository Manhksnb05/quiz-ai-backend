package com.hutech.quizbackend.model.response;

import lombok.Data;

@Data
public class CustomExamResponseDTO {
    private Long customExamId; // Frontend rất cần ID này để gọi API lấy câu hỏi thi
    private String title;
    private Integer timeLimit;
    private Integer questionCount;
    private String message;
}