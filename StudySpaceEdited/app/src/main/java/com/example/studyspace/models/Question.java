package com.example.studyspace.models;

import java.util.List;

public class Question {
    private String id; // ID của document trên Firebase
    private String questionText;
    private List<String> options;
    private int correctAnswerIndex; // 0, 1, 2, hoặc 3
    private String topic;
    private int level;

    // 1. BẮT BUỘC: Constructor rỗng cho Firebase
    public Question() { }

    // Constructor dùng trong App của bạn
    public Question(String questionText, List<String> options, int correctAnswerIndex, String topic, int level) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.topic = topic;
        this.level = level;
    }

    // Getter và Setter (Bắt buộc để Firebase đọc/ghi dữ liệu)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}