package com.hutech.quizbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content; // Nội dung câu hỏi

    // Lưu 4 đáp án dưới dạng List (JPA tự tạo ra 1 bảng phụ ẩn để lưu)
    @ElementCollection
    private List<String> options;

    private Integer correctIndex; // Đáp án đúng (0, 1, 2, 3)

    @Column(columnDefinition = "TEXT")
    private String explanation; // Giải thích chi tiết từ AI

    // Mối quan hệ: Nhiều Question thuộc về 1 Quiz
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
}