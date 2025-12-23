package com.example.studyspace.models;

import java.util.Date;

public class ChatMessage {
    private String senderId;
    private String message;
    private Date timestamp;

    // Thêm các trường mới cho tính năng Quiz
    private String type; // "text", "quiz" hoặc "exam"
    private String topic;
    private int level;
    private int limit;
    private String examId; // ID của bộ đề (cho type="exam")

    public ChatMessage() { }

    // Constructor chung cho tất cả các loại
    public ChatMessage(String senderId, String message, Date timestamp, String type, String examId, int level, int limit) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.examId = examId;
        this.level = level;
        this.limit = limit;
    }

    // Getter & Setter
    public String getType() { return type != null ? type : "text"; }
    public void setType(String type) { this.type = type; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}