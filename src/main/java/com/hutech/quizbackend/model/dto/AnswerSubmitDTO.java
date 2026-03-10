package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class AnswerSubmitDTO {
    private Long questionId;
    private String selectedOption;
}