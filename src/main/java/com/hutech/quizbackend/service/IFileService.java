package com.hutech.quizbackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    String extractText(MultipartFile file) throws Exception;
}
