package com.hutech.quizbackend.service.Impl;

import com.hutech.quizbackend.model.dto.ResultHistoryDTO;
import com.hutech.quizbackend.entity.Result;
import com.hutech.quizbackend.repository.ResultRepository;
import com.hutech.quizbackend.service.IUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Data
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final ResultRepository resultRepository;

    @Override
    public List<ResultHistoryDTO> getUserResultHistory(Long userId) {

        // 1. Lấy danh sách kết quả từ Database
        List<Result> results = resultRepository.findByUserIdOrderByCompletedAtDesc(userId);

        // 2. Map (Chuyển đổi) từ Entity sang DTO
        List<ResultHistoryDTO> historyList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (Result r : results) {
            ResultHistoryDTO dto = new ResultHistoryDTO();
            dto.setResultId(r.getId());

            // Lấy ID và Title từ bảng CustomExam thay vì Exam gốc
            if (r.getCustomExam() != null) {
                dto.setExamId(r.getCustomExam().getId());
                dto.setExamTitle(r.getCustomExam().getTitle());
            }

            dto.setScore(r.getScore());
            dto.setCorrectAnswers(r.getCorrectAnswers());
            dto.setTotalQuestions(r.getTotalQuestions());

            // Format ngày giờ cho đẹp mắt
            if (r.getCompletedAt() != null) {
                dto.setCompletedAt(r.getCompletedAt().format(formatter));
            }

            historyList.add(dto);
        }

        return historyList;
    }

    @Override
    @Transactional
    public void softDeleteResults(List<Long> resultIds) {
        List<Result> results = resultRepository.findAllById(resultIds);

        if (results.isEmpty()) {
            throw new RuntimeException("Không tìm thấy kết quả nào để xóa!");
        }

        results.forEach(result -> result.setActive(false));
        resultRepository.saveAll(results);
    }
}
