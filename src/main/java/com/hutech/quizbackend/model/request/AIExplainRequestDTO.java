package com.hutech.quizbackend.model.request;

import lombok.Data;

@Data
public class AIExplainRequestDTO {
    private Long questionId; // ID của câu hỏi cần giải thích
    private String userSelectedOption; // Đáp án người dùng đã chọn (VD: "A", "B", hoặc bỏ trống)
}