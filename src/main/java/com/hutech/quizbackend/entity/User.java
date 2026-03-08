package com.hutech.quizbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    private String role; // "ROLE_USER" hoặc "ROLE_ADMIN"

    private String provider; // Ghi chú: GOOGLE

    // Quan hệ 1-N: 1 User có thể tạo nhiều Exam
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Không in danh sách này ra JSON để tránh lặp
    @ToString.Exclude // Tránh lỗi lặp vô tận của Lombok
    private List<Exam> exams;

    // Quan hệ 1-N: 1 User có thể có nhiều Kết quả (Result)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<Result> results;
}