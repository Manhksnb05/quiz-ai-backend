package com.hutech.quizbackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionClientDTO { // Câu hỏi hiển thị cho người làm
    private Long id;
    private String questionContent;
    private List<String> options;
    private String answer; // Đáp án đúng (A/B/C/D) — dùng sau khi nộp bài
}