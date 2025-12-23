package com.example.studyspace;

import java.util.List;

public class ClassModel {
    private String classId;
    private String className;
    private List<String> member;
    private String userId;

    public ClassModel() { } // Bắt buộc có

    public ClassModel(String classId, String className, List<String> member, String userId) {
        this.classId = classId;
        this.className = className;
        this.member = member;
        this.userId = userId;
    }

    // Getter & Setter
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public List<String> getMember() { return member; }
    public void setMember(List<String> member) { this.member = member; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}