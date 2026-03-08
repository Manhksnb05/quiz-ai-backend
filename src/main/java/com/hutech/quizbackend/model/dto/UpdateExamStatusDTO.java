package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class UpdateExamStatusDTO {
    private String status; // Chứa chữ "Public" hoặc "Private"
}