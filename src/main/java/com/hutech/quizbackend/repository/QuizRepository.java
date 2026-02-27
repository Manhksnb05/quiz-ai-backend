package com.hutech.quizbackend.repository;

import com.hutech.quizbackend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // Tạm thời chưa cần viết thêm hàm gì, JpaRepository đã có sẵn save(), findAll()...
}