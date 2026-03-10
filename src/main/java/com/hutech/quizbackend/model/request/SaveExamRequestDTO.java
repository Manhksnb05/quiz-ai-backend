package com.hutech.quizbackend.model.request;

import lombok.Data;
import java.util.List;

@Data
public class SaveExamRequestDTO { // Hứng toàn bộ form lưu đề thi
    private String title;
    private List<QuestionRequestDTO> questions;
    // Tạm thời chưa gán UserID, sau này có tính năng Login (Security) ta sẽ lấy tự động từ Token
    private Long userId;
}
