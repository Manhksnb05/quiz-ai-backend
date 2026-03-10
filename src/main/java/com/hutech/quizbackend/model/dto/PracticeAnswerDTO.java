package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class PracticeAnswerDTO {
    private Long questionId;
    private String selectedOption;
}