package com.hutech.quizbackend.model.request;

import lombok.Data;
import java.util.List;

@Data
public class QuestionRequestDTO { // Hứng câu hỏi do AI bóc tách gửi lên
    private String question;
    private List<String> options;
    private String answer;
}
