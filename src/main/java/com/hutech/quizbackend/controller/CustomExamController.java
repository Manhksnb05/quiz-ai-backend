package com.hutech.quizbackend.controller;

import com.hutech.quizbackend.model.dto.*;
import com.hutech.quizbackend.model.request.CustomExamRequestDTO;
import com.hutech.quizbackend.model.request.SubmitCustomExamRequestDTO;
import com.hutech.quizbackend.model.response.CustomExamResponseDTO;
import com.hutech.quizbackend.service.Impl.CustomExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CustomExamController {

    @Autowired
    private CustomExamService customExamService;

    // 1. API: Tạo đề thi tùy chỉnh
    @PostMapping("/custom-exams")
    public ResponseEntity<?> createCustomExam(@RequestBody CustomExamRequestDTO request) {

        try {
            CustomExamResponseDTO response = customExamService.createCustomExam(request);
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. API: Lấy chi tiết đề thi tùy chỉnh để bắt đầu làm bài
    @GetMapping("/custom-exams/{id}/take")
    public ResponseEntity<?> takeCustomExam(@PathVariable Long id) {

        try {
            CustomExamTakeDTO examInfo = customExamService.getCustomExamForTake(id);
            return ResponseEntity.ok(examInfo);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. API: Nộp bài thi tùy chỉnh (Thi thật) và lưu điểm (Result)
    @PostMapping("/custom-exams/submit")
    public ResponseEntity<?> submitCustomExam(@RequestBody SubmitCustomExamRequestDTO request) {

        try {
            CustomExamResultDTO result = customExamService.submitCustomExam(request);
            return ResponseEntity.ok(result);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. API: Xóa mềm nhiều đề thi tùy chỉnh
    @DeleteMapping("/custom-exams")
    public ResponseEntity<String> deleteCustomExams(@RequestBody List<Long> ids) {
        try {
            customExamService.softDeleteCustomExams(ids);
            return ResponseEntity.ok("Đã xóa " + ids.size() + " đề thi tùy chỉnh thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. API: Xuất file Word cho đề thi tùy chỉnh
    @GetMapping("/custom-exams/{id}/export")
    public ResponseEntity<byte[]> exportCustomExam(@PathVariable Long id) {
        try {
            // Service xử lý toàn bộ logic phức tạp
            byte[] documentBytes = customExamService.exportCustomExamToWord(id);

            // Controller chỉ làm nhiệm vụ giao tiếp HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // Thiết lập tên file tải về
            headers.setContentDispositionFormData("attachment", "DeThi_Custom_" + id + ".docx");

            return new ResponseEntity<>(documentBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
