package com.hutech.quizbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exams")
@Data
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Quan hệ N-1: Nhiều Exam thuộc về 1 User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore // Chỉ lấy ID hoặc bỏ qua để dữ liệu API nhẹ hơn
    private User user;

    // Quan hệ 1-N: 1 Exam có nhiều Question
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Quản lý vòng lặp JSON cho Question
    @ToString.Exclude
    private List<Question> questions;

    // Quan hệ 1-N: 1 Exam gốc tạo ra nhiều CustomExam
    @OneToMany(mappedBy = "originExam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<CustomExam> customExams;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    @Column(name = "active")
    private Boolean active;

    @Column(name = "status") // Private / Public : Lưu trạng thái của bộ đề
    private String status;
}