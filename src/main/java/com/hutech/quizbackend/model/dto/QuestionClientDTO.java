package com.hutech.quizbackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionClientDTO { // Câu hỏi hiển thị cho người làm
    private Long id;
    private String questionContent;
    private List<String> options; // Chỉ trả về A, B, C, D (Không có đáp án đúng)
}