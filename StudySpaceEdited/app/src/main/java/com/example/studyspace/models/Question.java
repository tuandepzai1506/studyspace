package com.example.studyspace.models;

import com.google.firebase.firestore.Exclude;
import java.util.List;
import java.util.ArrayList; // Thêm import này

public class Question {
    @Exclude
    private String id;

    // --- CÁC TRƯỜNG DỮ LIỆU CŨ VÀ MỚI ---
    private String content;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String correctAnswer;

    private String questionText;
    private List<String> options;
    private String topic;
    private int level;
    private int correctAnswerIndex;

    // Constructor rỗng (BẮT BUỘC cho Firebase)
    public Question() { }

    // Constructor cũ (để tương thích)
    public Question(String content, String option1, String option2, String option3, String option4, String correctAnswer, String topic, int level) {
        this.content = content;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswer = correctAnswer;
        this.questionText = content; // Đồng bộ
        this.topic = topic;
        this.level = level;
    }

    // --- CONSTRUCTOR MỚI ĐƯỢC SỬ DỤNG BỞI AddEditQuestionActivity ---
    // Phiên bản này đảm bảo tất cả các trường đều được điền dữ liệu
    public Question(String questionText, List<String> options, int correctAnswerIndex, String topic, int level) {
        // 1. Gán các giá trị chính
        this.questionText = questionText;
        this.options = (options != null) ? options : new ArrayList<>();
        this.correctAnswerIndex = correctAnswerIndex;
        this.topic = topic;
        this.level = level;

        // 2. Đồng bộ với các trường cũ để đảm bảo Firestore lưu đúng
        this.content = questionText;

        // Gán các đáp án vào từng trường 'option' riêng lẻ
        if (!this.options.isEmpty()) {
            this.option1 = this.options.size() > 0 ? this.options.get(0) : null;
            this.option2 = this.options.size() > 1 ? this.options.get(1) : null;
            this.option3 = this.options.size() > 2 ? this.options.get(2) : null;
            this.option4 = this.options.size() > 3 ? this.options.get(3) : null;
        }

        // Quy ước đáp án đúng luôn là option1
        this.correctAnswer = this.option1;
    }


    // --- GETTERS & SETTERS ---
    // Đảm bảo các getter trả về dữ liệu nhất quán

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() {
        return (content != null) ? content : questionText;
    }
    public void setContent(String content) { this.content = content; }

    public String getQuestionText() {
        return (questionText != null) ? questionText : content;
    }
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}
