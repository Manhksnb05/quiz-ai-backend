package com.hutech.quizbackend.service.Impl;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.*;

/**
 * Parser trực tiếp — KHÔNG dùng AI → 100% chính xác.
 *
 * Hỗ trợ các cách đánh dấu đáp án đúng:
 *   *B. Nội dung          ← dấu * trước chữ cái
 *    B. Nội dung *        ← dấu * cuối dòng
 *    B. Nội dung [Đ]      ← ký hiệu [Đ] hoặc (Đ)
 *    B. Nội dung ✓        ← dấu tích
 *   Bảng đáp án cuối:    Câu 1: B
 */
@Component
public class QuizParser {

    public static class ParsedQuestion {
        public String question;
        public List<String> options = new ArrayList<>();
        public String answer; // null nếu chưa tìm được
    }

    public static class ParseResult {
        public List<ParsedQuestion> questions = new ArrayList<>();
        public boolean hasAnswerKey = false;
        public int answeredCount = 0;
    }

    // Marker đánh dấu đáp án đúng inline
    private static final Pattern MARKER = Pattern.compile(
            "(?i)\\[đáp\\s*án\\s*đúng\\]|\\[đ\\]|\\(đ\\)|✓|✔|\\[correct\\]|\\(correct\\)",
            Pattern.UNICODE_CASE
    );

    // Pattern option: nhận *B. / B. / *B) / B) với khoảng trắng tuỳ ý đầu dòng
    private static final Pattern OPT_PAT = Pattern.compile(
            "^\\s*(\\*?)\\s*([ABCD])[.)\\u002E]\\s*(.+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // Pattern câu hỏi: "Câu 1:", "1.", "1)"
    private static final Pattern Q_PAT = Pattern.compile(
            "^\\s*(?:câu\\s*)?(\\d+)\\s*[:.)]\\s*(.{4,})",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    public ParseResult tryParse(String text) {
        ParseResult result = new ParseResult();

        // 1. Tìm bảng đáp án ở cuối file (nếu có)
        Map<Integer, String> answerTable = extractAnswerTable(text);

        // 2. Tách câu hỏi + options
        List<ParsedQuestion> questions = splitQuestions(text);
        if (questions.isEmpty()) return result;

        // 3. Gán đáp án từ bảng cuối file
        for (int i = 0; i < questions.size(); i++) {
            String ans = answerTable.get(i + 1);
            if (ans != null) {
                questions.get(i).answer = ans;
                result.answeredCount++;
            }
        }

        // 4. Gán đáp án từ đánh dấu inline (nếu chưa có từ bảng)
        for (ParsedQuestion q : questions) {
            if (q.answer == null) {
                String ans = detectInlineAnswer(q);
                if (ans != null) {
                    q.answer = ans;
                    result.answeredCount++;
                }
            }
        }

        result.questions = questions;
        // hasAnswerKey = true nếu >= 80% câu có đáp án
        result.hasAnswerKey = result.answeredCount >= (int) Math.ceil(questions.size() * 0.8);
        return result;
    }

    // ── Tìm bảng đáp án cuối file ────────────────────────────────────────────
    private Map<Integer, String> extractAnswerTable(String text) {
        Map<Integer, String> map = new LinkedHashMap<>();
        String lower = text.toLowerCase();
        int start = -1;
        for (String marker : new String[]{"bảng đáp án", "đáp án:", "answer key", "====="}) {
            int idx = lower.lastIndexOf(marker);
            if (idx > text.length() / 2) { start = idx; break; }
        }
        if (start < 0) return map;

        Matcher m = Pattern.compile(
                "(?:câu\\s*)?(\\d+)\\s*[:\\-.]\\s*([ABCD])",
                Pattern.CASE_INSENSITIVE
        ).matcher(text.substring(start));
        while (m.find()) {
            int num = Integer.parseInt(m.group(1));
            if (num >= 1 && num <= 300) map.put(num, m.group(2).toUpperCase());
        }
        return map;
    }

    // ── Tách câu hỏi và options từ text ──────────────────────────────────────
    private List<ParsedQuestion> splitQuestions(String text) {
        List<ParsedQuestion> list = new ArrayList<>();
        String[] lines = text.split("\\n");
        ParsedQuestion cur = null;

        for (String raw : lines) {
            String lower = raw.toLowerCase();
            // Bỏ qua dòng bảng đáp án
            if (lower.contains("bảng đáp án") || raw.trim().startsWith("=====")) continue;

            Matcher om = OPT_PAT.matcher(raw);
            Matcher qm = Q_PAT.matcher(raw);

            boolean isOpt = om.find();
            boolean isQ   = !isOpt && qm.find();

            if (isQ) {
                if (cur != null && !cur.options.isEmpty()) list.add(cur);
                cur = new ParsedQuestion();
                cur.question = qm.group(2).trim();

            } else if (isOpt && cur != null) {
                boolean hasStar   = !om.group(1).isEmpty();
                String  letter    = om.group(2).toUpperCase();
                String  content   = om.group(3).trim();
                boolean hasMarker = MARKER.matcher(content).find();

                // Xoá marker và dấu * cuối khỏi nội dung hiển thị
                String clean = MARKER.matcher(content).replaceAll("").trim();
                clean = clean.replaceAll("\\s*\\*\\s*$", "").trim();

                cur.options.add(letter + ". " + clean);

                // Đánh dấu đáp án đúng
                if ((hasStar || hasMarker) && cur.answer == null) {
                    cur.answer = letter;
                }

            } else if (cur != null && cur.options.isEmpty() && !raw.trim().isEmpty()) {
                // Câu hỏi dài nhiều dòng
                cur.question += " " + raw.trim();
            }
        }

        if (cur != null && !cur.options.isEmpty()) list.add(cur);
        return list;
    }

    // ── Phát hiện dấu * cuối option (fallback) ───────────────────────────────
    private String detectInlineAnswer(ParsedQuestion q) {
        for (int i = 0; i < q.options.size(); i++) {
            String opt = q.options.get(i);
            if (opt.matches(".*\\s\\*$") || opt.endsWith("*")) {
                q.options.set(i, opt.replaceAll("\\s*\\*$", "").trim());
                return opt.substring(0, 1).toUpperCase();
            }
        }
        return null;
    }
}