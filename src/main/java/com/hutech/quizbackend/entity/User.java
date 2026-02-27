package com.hutech.quizbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    private String role; // "ROLE_USER" hoặc "ROLE_ADMIN"

    private String provider; // Ghi chú đăng nhập bằng GOOGLE
}