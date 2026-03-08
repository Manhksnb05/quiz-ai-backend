package com.hutech.quizbackend.model.request;

import lombok.Data;

@Data
public class CustomExamRequestDTO {
    private Long originExamId;
    private Integer timeLimit;
    private Integer questionCount;
    private String userEmail;

    // THÊM MỚI: Nhận tên đề thi người dùng tự đặt từ Frontend gửi lên
    private String title;
}