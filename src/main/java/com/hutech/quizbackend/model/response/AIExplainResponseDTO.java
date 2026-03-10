package com.hutech.quizbackend.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AIExplainResponseDTO {
    private String greeting;// Lời chào và thông báo đúng/sai (VD: "Chào em, rất tiếc câu này em chọn chưa chuẩn rồi!")

    @JsonProperty("isCorrect")
    private boolean isCorrect;      // Cờ đánh dấu đúng sai để Frontend dễ đổi màu (Xanh/Đỏ)
    private String coreExplanation; // Giải thích trọng tâm ngắn gọn
    private List<String> details;   // Danh sách các gạch đầu dòng giải thích chi tiết
    private String advice;          // Lời khuyên, động viên cuối cùng
}