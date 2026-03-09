package com.hutech.quizbackend.service;

import com.hutech.quizbackend.model.dto.CustomExamSummaryDTO;
import com.hutech.quizbackend.model.dto.CustomExamTakeDTO;
import com.hutech.quizbackend.model.request.CustomExamRequestDTO;
import com.hutech.quizbackend.model.response.CustomExamResponseDTO;
import com.hutech.quizbackend.model.dto.CustomExamResultDTO;
import com.hutech.quizbackend.model.request.SubmitCustomExamRequestDTO;

import java.util.List;

public interface ICustomExamService {

    CustomExamResponseDTO createCustomExam(CustomExamRequestDTO request);

    CustomExamResultDTO submitCustomExam(SubmitCustomExamRequestDTO request);

    CustomExamTakeDTO getCustomExamForTake(Long customExamId);

    void softDeleteCustomExams(List<Long> ids);

    byte[] exportCustomExamToWord(Long customExamId);

    List<CustomExamSummaryDTO> getUserCustomExams(Long userId);
}
