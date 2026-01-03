package com.example.studyspace;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class createClass extends AppCompatActivity {

    private Button buttonCreateClassMain;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.create_class);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Button mở popup
        buttonCreateClassMain = findViewById(R.id.create_class);
        if (buttonCreateClassMain != null) {
            buttonCreateClassMain.setOnClickListener(v -> showClassPopup());
        }
    }

    private void showClassPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View popupView = LayoutInflater.from(this)
                .inflate(R.layout.popup_class, null);
        builder.setView(popupView);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText etClassName = popupView.findViewById(R.id.edittext_limit1);
        EditText etMemberInput  = popupView.findViewById(R.id.edittext_limit2);
        Button btnCreate     = popupView.findViewById(R.id.button_create_class);

        btnCreate.setOnClickListener(v -> {
            String className = etClassName.getText().toString().trim();
            String memberInfo   = etMemberInput.getText().toString().trim();

            if (TextUtils.isEmpty(className)) {
                etClassName.setError("Vui lòng nhập tên lớp");
                return;
            }

            // Gọi hàm tạo lớp
            createNewClassOnFirebase(className, memberInfo, dialog);
        });
    }

    private void createNewClassOnFirebase(
            String className,
            String memberInfo,
            AlertDialog dialog) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        // 1. Tạo ID cho lớp học
        String classId = db.collection("classes").document().getId();

        // 2. KHỞI TẠO DANH SÁCH THÀNH VIÊN
        List<String> initialMembers = new ArrayList<>();
        initialMembers.add(userId); // Thêm giáo viên vào lớp đầu tiên

        // 3. Tạo Object ClassModel
        ClassModel newClass = new ClassModel(
                classId,
                className,
                initialMembers,
                userId
        );

        // 4. Lưu thông tin lớp học lên Firestore
        db.collection("classes")
                .document(classId)
                .set(newClass)
                .addOnSuccessListener(unused -> {
                    // Khi tạo lớp thành công, gọi hàm gửi tin nhắn chào mừng chứa ID lớp
                    sendWelcomeMessage(classId, userId, dialog);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tạo lớp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CreateClass", "Firestore error", e);
                });
    }

    private void sendWelcomeMessage(String classId, String senderId, AlertDialog dialog) {
        // Nội dung tin nhắn chứa ID lớp
        String messageContent = "Mã lớp học: " + classId;

        Map<String, Object> messageObj = new HashMap<>();
        messageObj.put("senderId", senderId);
        messageObj.put("message", messageContent);
        messageObj.put("timestamp", new Date());

        // Lưu vào: classes -> [classId] -> messages
        db.collection("classes").document(classId).collection("messages")
                .add(messageObj)
                .addOnSuccessListener(documentReference -> {
                    // Gửi tin nhắn xong mới báo thành công và đóng popup
                    Toast.makeText(this, "Tạo lớp thành công & Đã gửi mã lớp!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Tạo lớp xong nhưng lỗi gửi tin nhắn.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
    }
}