package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

// Import Firebase
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class login extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPass;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPass = findViewById(R.id.tv_forgot_pass);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvRegister.setOnClickListener(v -> {
            Intent myIntent = new Intent(login.this, logup.class);
            startActivity(myIntent);
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập Email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập Mật khẩu");
            return;
        }

        // Đăng nhập Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập Auth thành công, giờ kiểm tra Role
                            checkUserRole();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi";
                            Toast.makeText(login.this, "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // --- KIỂM TRA ROLE VÀ CHUYỂN MÀN HÌNH ---
    private void checkUserRole() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        // Giả sử bạn lưu thông tin user trong collection "users"
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String role = documentSnapshot.getString("role");

                        if (role != null) {
                            if (role.equals("teacher")) {
                                // === TRƯỜNG HỢP GIÁO VIÊN ===
                                Toast.makeText(login.this, "Xin chào Giáo viên!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(login.this, MainActivity.class);
                                startActivity(intent);
                            } else if (role.equals("student")) {
                                // === TRƯỜNG HỢP HỌC SINH ===
                                Toast.makeText(login.this, "Xin chào Học sinh!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(login.this, StudentActivity.class);
                                startActivity(intent);
                            }
                        }
                        Toast.makeText(login.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(login.this, "Lỗi kết nối Database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}