package com.hutech.quizbackend.repository;

import com.hutech.quizbackend.entity.CustomExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomExamRepository extends JpaRepository<CustomExam, Long> {

    // Kiểm tra xem tên đề thi đã tồn tại trong cùng 1 bộ đề gốc VÀ chưa bị xóa hay chưa
    boolean existsByTitleAndOriginExamIdAndActiveTrue(String title, Long originExamId);
}