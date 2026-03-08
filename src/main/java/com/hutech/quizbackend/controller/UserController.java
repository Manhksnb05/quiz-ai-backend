package com.hutech.quizbackend.controller;

import com.hutech.quizbackend.model.dto.ResultHistoryDTO;
import com.hutech.quizbackend.service.Impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. API: Lấy lịch sử làm bài của 1 User
    @GetMapping("/{userId}/results")
    public ResponseEntity<?> getUserResults(@PathVariable Long userId) {

        try {
            List<ResultHistoryDTO> history = userService.getUserResultHistory(userId);
            return ResponseEntity.ok(history);
        }
        catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // 2. API: Người dùng xóa NHIỀU lịch sử làm bài cùng lúc
    @DeleteMapping("/results")
    public ResponseEntity<String> deleteUserResults(@RequestBody List<Long> resultIds) {
        try {
            userService.softDeleteResults(resultIds);
            return ResponseEntity.ok("Đã xóa " + resultIds.size() + " lịch sử làm bài thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}