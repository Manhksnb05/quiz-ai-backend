package com.hutech.quizbackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamDetailPublicDTO {
    private Long id;
    private String title;
    private Integer totalQuestions;
    private String creatorName;
    private String createdAt;

    // Tái sử dụng DTO câu hỏi đã tạo ở bài trước (Bảo mật: Không chứa đáp án đúng)
    private List<QuestionClientDTO> questions;
}