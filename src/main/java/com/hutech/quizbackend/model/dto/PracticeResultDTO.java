package com.hutech.quizbackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class PracticeResultDTO {
    private int totalQuestions;
    private int correctCount;
    private int incorrectCount;
    private List<PracticeFeedbackDTO> details; // Danh sách trạng thái của từng câu
}