package com.example.studyspace.models;

public class Question {
    private String id;
    private String content;      // Nội dung câu hỏi
    private String option1;      // Đáp án A
    private String option2;      // Đáp án B
    private String option3;      // Đáp án C
    private String option4;      // Đáp án D
    private String correctAnswer; // Đáp án đúng (VD: "Option 1")
    private String topic;        // Chủ đề
    private int level;           // Độ khó

    // Constructor rỗng (Bắt buộc để Firebase hoạt động)
    public Question() { }

    // Constructor đầy đủ
    public Question(String content, String option1, String option2, String option3, String option4, String correctAnswer, String topic, int level) {
        this.content = content;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswer = correctAnswer;
        this.topic = topic;
        this.level = level;
    }

    // --- GETTERS AND SETTERS ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

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
}