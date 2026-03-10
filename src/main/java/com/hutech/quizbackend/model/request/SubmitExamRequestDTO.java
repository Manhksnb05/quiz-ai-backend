package com.hutech.quizbackend.model.request;

import com.hutech.quizbackend.model.dto.AnswerSubmitDTO;
import lombok.Data;
import java.util.List;

@Data
public class SubmitExamRequestDTO {
    private Long userId; // ID của người nộp bài
    private Long examId; // ID của đề thi
    private List<AnswerSubmitDTO> answers; // Danh sách các câu trả lời
}