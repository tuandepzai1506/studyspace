package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class login extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

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
            Toast.makeText(this, "Chuyển sang màn hình Đăng ký", Toast.LENGTH_SHORT).show();
            // TODO: Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            // startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Kiểm tra dữ liệu đầu vào (Validate)
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

        // 2. TODO: Gọi API hoặc kiểm tra Database ở đây (Firebase / SQL)
        // Đây là code giả lập đăng nhập thành công:
        if (email.equals("admin@gmail.com") && password.equals("123456")) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển sang màn hình Ngân hàng câu hỏi
            Intent intent = new Intent(login.this, MainActivity.class);
            startActivity(intent);
            finish(); // Đóng LoginActivity để user không back lại được
        } else {
            Toast.makeText(this, "Email hoặc mật khẩu sai!", Toast.LENGTH_SHORT).show();
        }
    }
}