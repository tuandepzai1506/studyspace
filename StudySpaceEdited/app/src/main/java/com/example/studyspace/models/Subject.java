package com.example.studyspace.models;

public class Subject {
    private String id; // Chính là Document ID (toan, van...)
    private String name;
    private String icon;

    public Subject() {} // Constructor rỗng cho Firebase

    public Subject(String id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}