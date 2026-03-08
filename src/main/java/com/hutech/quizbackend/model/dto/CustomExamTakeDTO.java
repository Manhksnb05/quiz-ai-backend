package com.hutech.quizbackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class CustomExamTakeDTO { // Gói toàn bộ thông tin đề thi
    private Long customExamId;
    private String title;
    private Integer timeLimit;
    private List<QuestionClientDTO> questions;
}