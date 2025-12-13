package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class logup extends AppCompatActivity {
    private TextInputEditText etFullname, etEmail, etPassword, etStudentId, etClassId;
    private MaterialButton btnContinue;
    private TextView tvBackLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logup);
        etFullname = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etStudentId = findViewById(R.id.et_student_id);
        etClassId = findViewById(R.id.et_class_id);
        btnContinue = findViewById(R.id.btn_continue);
        tvBackLogin = findViewById(R.id.tv_back_login);

        if (tvBackLogin != null) {
            tvBackLogin.setOnClickListener(v -> {
                        runAnimationAndSwitchActivity(v, login.class);
                    }
            );
        }
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v ->{
                runAnimationAndSwitchActivity(v, FaceRegistrationActivity.class);
            } );
        }
    }
    private void handleLogup() {
        // Lấy dữ liệu
        String name = etFullname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String msv = etStudentId.getText().toString().trim();
        String lop = etClassId.getText().toString().trim();

        // 1. Validate (Kiểm tra rỗng)
        if (TextUtils.isEmpty(name)) {
            etFullname.setError("Vui lòng nhập họ tên");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập Email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        if (TextUtils.isEmpty(msv)) {
            etStudentId.setError("Vui lòng nhập Mã SV");
            return;
        }
        if (TextUtils.isEmpty(lop)) {
            etClassId.setError("Vui lòng nhập Lớp tín chỉ");
            return;
        }
        if (msv.equals("B21DCCN001")) {
            Toast.makeText(this, "Tài khoản (Mã SV) đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Hậu sự kiện: Chuyển tới giao diện đăng ký FaceID
        Toast.makeText(this, "Thông tin hợp lệ! Chuyển sang đăng ký FaceID...", Toast.LENGTH_SHORT).show();

        // TODO: Tạo Activity FaceRegistrationActivity rồi mở dòng dưới ra
        // Intent intent = new Intent(RegisterStudentActivity.this, FaceRegistrationActivity.class);
        // intent.putExtra("MSV", msv); // Truyền Mã SV sang để đăng ký FaceID
        // startActivity(intent);
    }


    private void runAnimationAndSwitchActivity(android.view.View v, Class<?> destinationActivity) {
        try {
            Animation flashAnim = AnimationUtils.loadAnimation(logup.this, R.anim.button_press_animation);
            v.startAnimation(flashAnim);

            flashAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent intent = new Intent(logup.this, destinationActivity);
                    startActivity(intent);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        } catch (Exception e) {
            // Nếu lỗi animation thì chuyển trang luôn
            Intent intent = new Intent(logup.this, destinationActivity);
            startActivity(intent);
        }
    }
}
