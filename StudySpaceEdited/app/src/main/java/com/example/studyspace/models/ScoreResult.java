package com.example.studyspace.models;

import java.util.Date;

public class ScoreResult {
    private String topic;
    private double score;
    private Date timestamp;

    public ScoreResult() { } // Bắt buộc cho Firebase

    public ScoreResult(String topic, double score, Date timestamp) {
        this.topic = topic;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getTopic() { return topic; }
    public double getScore() { return score; }
    public Date getTimestamp() { return timestamp; }
}