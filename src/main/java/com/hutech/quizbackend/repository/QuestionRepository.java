package com.hutech.quizbackend.repository;

import com.hutech.quizbackend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    // Hàm này giúp lấy toàn bộ câu hỏi thuộc về 1 đề thi cụ thể
    List<Question> findByQuizId(Long quizId);
}