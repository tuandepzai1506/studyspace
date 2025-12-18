package com.example.studyspace;

public class ClassModel {

    private String classId;
    private String className;
    private String member; // Biến này bạn dùng để lưu mô tả hoặc môn học?
    private String userId;

    // 1. QUAN TRỌNG: Constructor rỗng (Bắt buộc cho Firebase)
    public ClassModel() {
        // Để trống, không viết gì ở đây cũng được
    }

    // 2. Constructor đầy đủ (Dùng khi bạn tạo lớp mới để đẩy lên)
    public ClassModel(String classId, String className, String member, String userId) {
        this.classId = classId;
        this.className = className;
        this.member = member;
        this.userId = userId;
    }

    // 3. Getter và Setter (Bắt buộc phải có đủ để Firebase đọc/ghi)

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) { // Firebase cần cái này
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) { // Firebase cần cái này
        this.className = className;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) { // Firebase cần cái này
        this.member = member;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}