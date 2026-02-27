package com.hutech.quizbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // Tên đề thi (ví dụ: "Đề ôn tập file A")

    private LocalDateTime createdAt = LocalDateTime.now();

    // Mối quan hệ: 1 User có thể tạo nhiều Quiz
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}