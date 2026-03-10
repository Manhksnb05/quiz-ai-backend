package com.hutech.quizbackend.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hutech.quizbackend.model.request.QuestionRequestDTO;
import com.hutech.quizbackend.model.response.AIExplainResponseDTO;
import com.hutech.quizbackend.entity.Question;
import com.hutech.quizbackend.repository.QuestionRepository;
import com.hutech.quizbackend.service.IGeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GeminiService implements IGeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired private QuestionRepository questionRepository;
    @Autowired private QuizParser quizParser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // TẠO ĐỀ THI TỪ FILE
    // Logic:
    //   - Câu CÓ đánh dấu (*B., [Đ], ✓, bảng đáp án) → Parser 100% chính xác
    //   - Câu CHƯA đánh dấu                           → AI suy luận
    //   - Ghép 2 kết quả lại thành JSON cuối cùng
    // =========================================================================
    @Override
    public String generateAndSaveQuiz(String promptText) {

        // ── BƯỚC 1: Parser đọc toàn bộ file ──────────────────────────────────
        QuizParser.ParseResult parsed;
        try {
            parsed = quizParser.tryParse(promptText);
        } catch (Exception e) {
            parsed = new QuizParser.ParseResult();
        }

        List<QuizParser.ParsedQuestion> answeredList   = new ArrayList<>();
        List<QuizParser.ParsedQuestion> unansweredList = new ArrayList<>();

        for (QuizParser.ParsedQuestion q : parsed.questions) {
            if (q.answer != null) answeredList.add(q);
            else                  unansweredList.add(q);
        }

        System.out.println("[QuizParser] Đã đánh dấu: " + answeredList.size()
                + " | Chưa đánh dấu: " + unansweredList.size());

        // ── BƯỚC 2: Nếu tất cả đã có đáp án → trả về luôn, không gọi AI ─────
        if (!answeredList.isEmpty() && unansweredList.isEmpty()) {
            try {
                System.out.println("[QuizParser] 100% đánh dấu → bỏ qua AI hoàn toàn");
                return buildJson(answeredList);
            } catch (Exception e) { /* fallback AI */ }
        }

        // ── BƯỚC 3: Nếu không có câu nào đánh dấu → gọi AI toàn bộ ─────────
        if (answeredList.isEmpty()) {
            System.out.println("[GeminiService] Không có đánh dấu → gọi AI toàn bộ");
            return callAI(promptText, Collections.emptyList());
        }

        // ── BƯỚC 4: Có MỘT PHẦN đánh dấu → chỉ gửi câu CHƯA đánh dấu cho AI
        // Xây dựng text chỉ gồm các câu chưa có đáp án
        StringBuilder unansweredText = new StringBuilder();
        int idx = 1;
        for (QuizParser.ParsedQuestion q : unansweredList) {
            unansweredText.append("Câu ").append(idx++).append(": ").append(q.question).append("\n");
            for (String o : q.options) unansweredText.append(o).append("\n");
            unansweredText.append("\n");
        }

        System.out.println("[GeminiService] Gửi " + unansweredList.size() + " câu cho AI xử lý");
        return callAI(unansweredText.toString(), answeredList);
    }

    // ── Gọi AI và ghép kết quả với answeredList ───────────────────────────────
    private String callAI(String inputText, List<QuizParser.ParsedQuestion> answeredList) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Bạn là CHUYÊN GIA TRÍCH XUẤT ĐỀ THI.\n\n");
            sb.append("QUY TẮC:\n");
            sb.append("- Nếu file có đánh dấu đáp án (*A, [Đ], ✓, bảng đáp án cuối) → BẮT BUỘC dùng đó.\n");
            sb.append("- Nếu KHÔNG có đánh dấu → suy luận chọn đáp án đúng nhất.\n");
            sb.append("- KHÔNG bịa câu hỏi không có trong tài liệu.\n");
            sb.append("- Xáo trộn vị trí đáp án đúng, không phải lúc nào cũng là A.\n\n");
            sb.append("Chỉ trả về JSON, KHÔNG giải thích, KHÔNG ```json:\n");
            sb.append("[{\"question\":\"...\",\"options\":[\"A. ...\",\"B. ...\",\"C. ...\",\"D. ...\"],\"answer\":\"B\"}]\n\n");
            sb.append("=== TÀI LIỆU ===\n");
            sb.append(inputText);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", sb.toString())))),
                    "generationConfig", Map.of("temperature", 0.1, "topP", 0.7, "topK", 20)
            );

            String rawResponse = new RestTemplate().postForObject(url, requestBody, String.class);
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            String quizJson = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text").asText();
            quizJson = quizJson.replaceAll("```json", "").replaceAll("```", "").trim();

            // Ghép: câu đã Parser (trước) + câu AI (sau)
            if (!answeredList.isEmpty()) {
                try {
                    ArrayNode aiArr    = (ArrayNode) objectMapper.readTree(quizJson);
                    ArrayNode finalArr = objectMapper.createArrayNode();
                    // Câu đã có đáp án từ Parser
                    for (QuizParser.ParsedQuestion q : answeredList) {
                        ObjectNode node = objectMapper.createObjectNode();
                        node.put("question", q.question);
                        ArrayNode opts = objectMapper.createArrayNode();
                        for (String o : q.options) opts.add(o);
                        node.set("options", opts);
                        node.put("answer", q.answer);
                        finalArr.add(node);
                    }
                    // Câu AI vừa tạo
                    finalArr.addAll(aiArr);
                    return objectMapper.writeValueAsString(finalArr);
                } catch (Exception e) {
                    // Nếu ghép lỗi → trả AI bình thường
                }
            }

            return quizJson;

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Lỗi khi gọi AI: " + e.getMessage() + "\"}";
        }
    }

    // ── Chuyển danh sách ParsedQuestion → JSON string ─────────────────────────
    private String buildJson(List<QuizParser.ParsedQuestion> list) throws Exception {
        ArrayNode arr = objectMapper.createArrayNode();
        for (QuizParser.ParsedQuestion q : list) {
            if (q.answer == null || q.question == null || q.options.size() < 2) continue;
            ObjectNode node = objectMapper.createObjectNode();
            node.put("question", q.question);
            ArrayNode opts = objectMapper.createArrayNode();
            for (String o : q.options) opts.add(o);
            node.set("options", opts);
            node.put("answer", q.answer);
            arr.add(node);
        }
        return objectMapper.writeValueAsString(arr);
    }

    // =========================================================================
    // GIẢI THÍCH ĐÁP ÁN
    // =========================================================================
    @Override
    public AIExplainResponseDTO explainQuestion(Long questionId, String userSelectedOption) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        boolean isUserCorrect = userSelectedOption != null &&
                q.getAnswer().trim().equalsIgnoreCase(userSelectedOption.trim());

        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một Bộ não thiên tài. Hãy giải thích câu hỏi trắc nghiệm sau.\n");
        prompt.append("Câu hỏi: ").append(q.getQuestion()).append("\n");
        prompt.append("Các đáp án: ").append(String.join(", ", q.getOptions())).append("\n");
        prompt.append("Đáp án đúng: ").append(q.getAnswer()).append("\n");
        prompt.append("Học sinh chọn: ").append(userSelectedOption != null ? userSelectedOption : "Không chọn").append("\n");
        prompt.append("\nBẮT BUỘC trả về JSON (không kèm văn bản khác):\n");
        prompt.append("{\n");
        prompt.append("  \"greeting\": \"Lời chào và nhận xét ngắn\",\n");
        prompt.append("  \"isCorrect\": ").append(isUserCorrect).append(",\n");
        prompt.append("  \"coreExplanation\": \"Tại sao đáp án đúng lại chính xác\",\n");
        prompt.append("  \"details\": [\"Phân tích A\", \"Phân tích B\", \"Kiến thức mở rộng\"],\n");
        prompt.append("  \"advice\": \"Lời động viên\"\n");
        prompt.append("}");

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt.toString())))),
                    "generationConfig", Map.of("temperature", 0.3)
            );
            String rawResponse = new RestTemplate().postForObject(url, requestBody, String.class);
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            String aiText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text").asText();
            aiText = aiText.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(aiText, AIExplainResponseDTO.class);

        } catch (Exception e) {
            e.printStackTrace();
            AIExplainResponseDTO err = new AIExplainResponseDTO();
            err.setGreeting("Lỗi kết nối Gia sư AI.");
            err.setCoreExplanation("Hệ thống đang quá tải: " + e.getMessage());
            return err;
        }
    }

    // =========================================================================
    // SINH CÂU HỎI THÍCH ỨNG
    // =========================================================================
    @Override
    public List<QuestionRequestDTO> generateAdaptiveQuestions(List<String> weakQuestions, int count) {
        if (weakQuestions == null || weakQuestions.isEmpty()) return new ArrayList<>();

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Bạn là giáo viên IT. Học sinh thường xuyên làm sai:\n");
            for (int i = 0; i < weakQuestions.size(); i++) {
                prompt.append(i + 1).append(". ").append(weakQuestions.get(i)).append("\n");
            }
            prompt.append("\nSinh ĐÚNG ").append(count).append(" câu hỏi mới cùng phạm vi, mỗi câu 1 đáp án đúng, 3 đáp án sai hợp lý.\n");
            prompt.append("Chỉ trả về JSON:\n[{\"question\":\"...\",\"options\":[\"A. ...\",\"B. ...\",\"C. ...\",\"D. ...\"],\"answer\":\"A\"}]");

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt.toString())))),
                    "generationConfig", Map.of("temperature", 0.4)
            );
            String rawResponse = new RestTemplate().postForObject(url, requestBody, String.class);
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            String aiText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text").asText();
            aiText = aiText.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode arrayNode = objectMapper.readTree(aiText);
            return objectMapper.convertValue(arrayNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionRequestDTO.class));

        } catch (Exception e) {
            System.err.println("Lỗi sinh câu hỏi thích ứng: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}