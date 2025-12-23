package com.example.studyspace.models;

import java.util.Date;

public class ScoreResult {
    private String topic; // Tên bộ đề hoặc chủ đề
    private double score;
    private Date timestamp;
    private String examName; // Tên bộ đề
    private String examId; // ID bộ đề
    private int totalQuestions;
    private int correctAnswers;

    public ScoreResult() { } // Bắt buộc cho Firebase

    public ScoreResult(String topic, double score, Date timestamp) {
        this.topic = topic;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getTopic() { return topic; }
    public double getScore() { return score; }
    public Date getTimestamp() { return timestamp; }
    
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    
    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }
    
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
}