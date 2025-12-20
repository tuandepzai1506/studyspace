package com.example.studyspace.models;

import java.util.Date;

public class ChatMessage {
    private String senderId;
    private String message;
    private Date timestamp;

    // Thêm các trường mới cho tính năng Quiz
    private String type; // "text" hoặc "quiz"
    private String topic;
    private int level;
    private int limit;

    public ChatMessage() { }

    // Constructor đầy đủ
    public ChatMessage(String senderId, String message, Date timestamp, String type, String topic, int level, int limit) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.topic = topic;
        this.level = level;
        this.limit = limit;
    }

    // Getter & Setter cho các trường mới
    public String getType() { return type != null ? type : "text"; }
    public void setType(String type) { this.type = type; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    // ... Giữ nguyên các Getter/Setter cũ ...
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}