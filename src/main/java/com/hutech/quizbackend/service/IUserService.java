package com.hutech.quizbackend.service;

import com.hutech.quizbackend.model.dto.ResultHistoryDTO;

import java.util.List;

public interface IUserService {

    List<ResultHistoryDTO> getUserResultHistory(Long userId);

    void softDeleteResults(List<Long> resultIds);
}
