package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

// 1. Import thư viện Firebase
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class login extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPass;

    // 2. Khai báo biến Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // 3. Khởi tạo Firebase Auth ngay khi màn hình chạy
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ View
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPass = findViewById(R.id.tv_forgot_pass);

        // Xử lý sự kiện Đăng nhập
        btnLogin.setOnClickListener(v -> handleLogin());

        // Xử lý sự kiện chuyển sang màn hình Đăng ký
        tvRegister.setOnClickListener(v -> {
            Intent myIntent = new Intent(login.this, logup.class);
            startActivity(myIntent);
        });
    }
    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào (Validate)
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập Email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập Mật khẩu");
            etPassword.requestFocus();
            return;
        }

        // Gọi hàm đăng nhập của Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập THÀNH CÔNG
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // Chuyển sang màn hình chính
                            Intent intent = new Intent(login.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Đóng màn hình login lại để user không back về được
                        } else {
                            // Đăng nhập THẤT BẠI
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                            Toast.makeText(login.this, "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}