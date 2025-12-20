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
        EditText etMember  = popupView.findViewById(R.id.edittext_limit2);
        Button btnCreate     = popupView.findViewById(R.id.button_create_class);

        btnCreate.setOnClickListener(v -> {
            String className = etClassName.getText().toString().trim();
            String member   = etMember.getText().toString().trim();

            if (TextUtils.isEmpty(className)) {
                etClassName.setError("Vui lòng nhập tên lớp");
                return;
            }

            // Gọi hàm tạo lớp
            createNewClassOnFirebase(className, member, dialog);
        });
    }

    private void createNewClassOnFirebase(
            String className,
            String member,
            AlertDialog dialog) {

        FirebaseUser user = mAuth.getCurrentUser();

        String userId = user.getUid();

        // Tạo ID cho lớp học
        String classId = db.collection("classes").document().getId();
        ClassModel newClass = new ClassModel(
                classId,
                className,
                member,
                userId
        );

        // Lưu lên Firestore
        db.collection("classes")
                .document(classId)
                .set(newClass)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Tạo lớp thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // Đóng popup
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CreateClass", "Firestore error", e);
                });
    }
}