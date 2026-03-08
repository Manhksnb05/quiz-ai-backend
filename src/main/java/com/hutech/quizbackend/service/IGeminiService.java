package com.hutech.quizbackend.service;

import com.hutech.quizbackend.model.response.AIExplainResponseDTO;

public interface IGeminiService {

    String generateAndSaveQuiz(String promptText);

    AIExplainResponseDTO explainQuestion(Long questionId, String userSelectedOption);
}
