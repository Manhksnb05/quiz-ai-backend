package com.hutech.quizbackend.service;

import com.hutech.quizbackend.model.dto.ResultHistoryDTO;
import com.hutech.quizbackend.model.dto.UserDTO;

import java.util.List;

public interface IUserService {

    List<ResultHistoryDTO> getUserResultHistory(Long userId);

    void softDeleteResults(List<Long> resultIds);

    UserDTO getUserById(Long userId);
}
