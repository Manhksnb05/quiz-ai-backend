package com.hutech.quizbackend.service.Impl;

import com.hutech.quizbackend.entity.*;
import com.hutech.quizbackend.model.dto.*;
import com.hutech.quizbackend.model.request.CustomExamRequestDTO;
import com.hutech.quizbackend.model.request.SubmitCustomExamRequestDTO;
import com.hutech.quizbackend.model.response.CustomExamResponseDTO;
import com.hutech.quizbackend.repository.*;
import com.hutech.quizbackend.service.ICustomExamService;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomExamService implements ICustomExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private CustomExamRepository customExamRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResultRepository resultRepository;

    // Tính năng: Tạo và lưu bài thi tùy chỉnh
    @Override
    @Transactional
    public CustomExamResponseDTO createCustomExam(CustomExamRequestDTO request) {
        // 1. Tìm bộ đề gốc
        Exam originExam = examRepository.findById(request.getOriginExamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ đề gốc!"));

        // 2. Xử lý tên đề thi tùy chỉnh
        String customTitle = request.getTitle();
        if (customTitle == null || customTitle.trim().isEmpty()) {
            // Nếu Frontend không gửi tên, ta tạo tên mặc định (kèm timestamp để đảm bảo luôn Unique)
            customTitle = "Thi thử: " + originExam.getTitle() + " - " + System.currentTimeMillis();
        } else {
            customTitle = customTitle.trim();
        }

        // 3. KIỂM TRA TRÙNG LẶP TÊN (Nghiệp vụ mới nâng cấp)
        boolean isExist = customExamRepository.existsByTitleAndOriginExamIdAndActiveTrue(customTitle, originExam.getId());
        if (isExist) {
            throw new RuntimeException("Tên đề thi '" + customTitle + "' đã tồn tại trong bộ đề này. Vui lòng chọn tên khác!");
        }

        // 4. Lấy danh sách câu hỏi & kiểm tra số lượng
        List<Question> allQuestions = originExam.getQuestions();
        int actualCount = request.getQuestionCount();
        if (actualCount > allQuestions.size()) {
            actualCount = allQuestions.size();
        }

        // 5. Xáo trộn và cắt lấy đúng số lượng
        Collections.shuffle(allQuestions);
        List<Question> selectedQuestions = allQuestions.stream()
                .limit(actualCount)
                .collect(Collectors.toList());

        // 6. Trích xuất ID
        String selectedIds = selectedQuestions.stream()
                .map(q -> String.valueOf(q.getId()))
                .collect(Collectors.joining(","));

        // 7. Tạo và lưu CustomExam
        CustomExam customExam = new CustomExam();
        customExam.setTitle(customTitle); // Dùng tên đã được xử lý ở bước 2
        customExam.setTimeLimit(request.getTimeLimit());
        customExam.setQuestionCount(actualCount);
        customExam.setSelectedQuestionIds(selectedIds);
        customExam.setOriginExam(originExam);
        customExam.setUserEmail(request.getUserEmail());

        customExam.setActive(true); // Thuộc tính phục vụ Xóa mềm

        CustomExam savedExam = customExamRepository.save(customExam);

        // 8. Trả về Response
        CustomExamResponseDTO response = new CustomExamResponseDTO();
        response.setCustomExamId(savedExam.getId());
        response.setTitle(savedExam.getTitle());
        response.setTimeLimit(savedExam.getTimeLimit());
        response.setQuestionCount(savedExam.getQuestionCount());
        response.setMessage("Tạo đề thi tùy chỉnh thành công!");

        return response;
    }

    // Tính năng: Chấm điểm bài thi tùy chỉnh và Lưu lịch sử
    @Transactional // Rollback nếu có lỗi xảy ra trong quá trình lưu
    @Override
    public CustomExamResultDTO submitCustomExam(SubmitCustomExamRequestDTO request) {
        // 1. Tìm User và CustomExam trong Database để chuẩn bị tạo mối quan hệ
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng!"));

        CustomExam customExam = customExamRepository.findById(request.getCustomExamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi này!"));

        // 2. Chấm điểm bài làm
        int correctCount = 0;
        int totalQuestions = request.getAnswers().size();

        for (AnswerSubmitDTO ans : request.getAnswers()) {
            Question q = questionRepository.findById(ans.getQuestionId()).orElse(null);

            if (q != null && q.getAnswer() != null && ans.getSelectedOption() != null) {
                // So sánh đáp án (Bỏ qua hoa thường và khoảng trắng thừa)
                if (q.getAnswer().trim().equalsIgnoreCase(ans.getSelectedOption().trim())) {
                    correctCount++;
                }
            }
        }

        // 3. Quy đổi sang thang điểm 10 (Làm tròn 2 chữ số thập phân)
        double score = totalQuestions == 0 ? 0 : (double) correctCount / totalQuestions * 10;
        double finalScore = (double) Math.round(score * 100) / 100;

        // 4. Lưu kết quả vào Database
        Result result = new Result();
        result.setUser(user);
        result.setCustomExam(customExam); // Liên kết với đề thi tùy chỉnh
        result.setScore(finalScore);
        result.setCorrectAnswers(correctCount);
        result.setTotalQuestions(totalQuestions);

        result.setActive(true);

        Result savedResult = resultRepository.save(result);

        // 5. Đóng gói kết quả trả về cho Frontend
        CustomExamResultDTO resultDTO = new CustomExamResultDTO();
        resultDTO.setResultId(savedResult.getId());
        resultDTO.setTotalQuestions(totalQuestions);
        resultDTO.setCorrectAnswers(correctCount);
        resultDTO.setScore(finalScore);

        return resultDTO;
    }

    // Tính năng: Lấy chi tiết đề thi tùy chỉnh để User làm bài
    @Override
    public CustomExamTakeDTO getCustomExamForTake(Long customExamId) {
        // 1. Tìm Custom Exam trong DB
        CustomExam customExam = customExamRepository.findById(customExamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi này!"));

        // 2. Tách chuỗi ID "8,2,1,10,9" thành danh sách các số Long
        String idsStr = customExam.getSelectedQuestionIds();
        List<Long> questionIds = Arrays.stream(idsStr.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // 3. Query DB lấy tất cả các câu hỏi này lên (DB sẽ trả về lộn xộn hoặc tăng dần)
        List<Question> questionsFromDb = questionRepository.findAllById(questionIds);

        // 4. Đưa vào Map để truy xuất nhanh theo ID
        Map<Long, Question> questionMap = questionsFromDb.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 5. Duyệt lại theo đúng thứ tự của questionIds để tạo danh sách DTO
        List<QuestionClientDTO> questionClientDTOs = new ArrayList<>();
        for (Long id : questionIds) {
            Question q = questionMap.get(id);
            if (q != null) {
                QuestionClientDTO qDto = new QuestionClientDTO();
                qDto.setId(q.getId());
                qDto.setQuestionContent(q.getQuestion());
                qDto.setOptions(q.getOptions());
                questionClientDTOs.add(qDto);
            }
        }

        // 6. Đóng gói vào DTO tổng
        CustomExamTakeDTO response = new CustomExamTakeDTO();
        response.setCustomExamId(customExam.getId());
        response.setTitle(customExam.getTitle());
        response.setTimeLimit(customExam.getTimeLimit());
        response.setQuestions(questionClientDTOs);

        return response;
    }

    @Transactional
    @Override
    public void softDeleteCustomExams(List<Long> ids) {
        List<CustomExam> customExams = customExamRepository.findAllById(ids);

        if (customExams.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đề thi tùy chỉnh nào để xóa!");
        }

        customExams.forEach(exam -> exam.setActive(false));
        customExamRepository.saveAll(customExams);
    }

    // Tính năng: Xuất đề thi tùy chỉnh ra file Word (.docx)
    @Override
    public byte[] exportCustomExamToWord(Long customExamId) {
        // 1. Tìm CustomExam
        CustomExam customExam = customExamRepository.findById(customExamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi tùy chỉnh!"));

        // 2. Lấy danh sách câu hỏi THEO ĐÚNG THỨ TỰ ngẫu nhiên đã lưu
        String idsStr = customExam.getSelectedQuestionIds();
        List<Long> questionIds = Arrays.stream(idsStr.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<Question> questionsFromDb = questionRepository.findAllById(questionIds);
        Map<Long, Question> questionMap = questionsFromDb.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<Question> orderedQuestions = new ArrayList<>();
        for (Long id : questionIds) {
            if (questionMap.containsKey(id)) {
                orderedQuestions.add(questionMap.get(id));
            }
        }

        // 3. Sử dụng Apache POI để vẽ file Word
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Tiêu đề
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setText("SMART EXAMS - ĐỀ THI: " + customExam.getTitle().toUpperCase());

            // Thông tin thời gian & Số câu hỏi
            XWPFParagraph meta = document.createParagraph();
            meta.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun metaRun = meta.createRun();
            metaRun.setItalic(true);
            metaRun.setText("Thời gian: " + customExam.getTimeLimit() + " phút | Tổng số câu: " + customExam.getQuestionCount());

            document.createParagraph(); // Thêm một dòng trống

            // In nội dung câu hỏi
            int index = 1;
            for (Question q : orderedQuestions) {
                // In câu hỏi
                XWPFParagraph p = document.createParagraph();
                XWPFRun qRun = p.createRun();
                qRun.setBold(true);
                qRun.setText("Câu " + (index++) + ": " + q.getQuestion());

                // In các đáp án A, B, C, D
                for (String opt : q.getOptions()) {
                    XWPFParagraph optP = document.createParagraph();
                    optP.setIndentationLeft(400); // Thụt đầu dòng
                    XWPFRun optRun = optP.createRun();
                    optRun.setText(opt);
                }
                document.createParagraph(); // Khoảng trống giữa 2 câu
            }

            // Ghi dữ liệu ra mảng byte
            document.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi trong quá trình tạo file Word: " + e.getMessage());
        }
    }
}
