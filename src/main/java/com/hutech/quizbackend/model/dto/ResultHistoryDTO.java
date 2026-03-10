package com.hutech.quizbackend.model.dto;

import lombok.Data;
import java.time.format.DateTimeFormatter;

@Data
public class ResultHistoryDTO {
    private Long resultId;
    private Long examId;
    private String examTitle;       // Frontend rất cần tên đề thi để hiển thị
    private double score;
    private int correctAnswers;
    private int totalQuestions;
    private String completedAt;     // Đổi ngày giờ thành chuỗi định dạng đẹp
}