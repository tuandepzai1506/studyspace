package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class logup extends AppCompatActivity {
    private TextInputEditText etFullname, etEmail, etPassword, etStudentId, etClassId;
    private MaterialButton btnRegister;
    private TextView tvBackLogin;

    // Khai báo Firebase Auth và Firestore
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Khai báo thêm RadioGroup và Layout bọc ô nhập (để ẩn hiện)
    private RadioGroup rgRole;
    private TextInputLayout tilStudentId, tilClassId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logup);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ View (Bổ sung phần còn thiếu)
        etFullname = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etStudentId = findViewById(R.id.et_student_id);
        etClassId = findViewById(R.id.et_class_id);
        btnRegister = findViewById(R.id.btn_continue);
        tvBackLogin = findViewById(R.id.tv_back_login);

        // Ánh xạ RadioGroup và TextInputLayout
        rgRole = findViewById(R.id.rg_role);
        tilStudentId = findViewById(R.id.til_student_id);
        tilClassId = findViewById(R.id.til_class_id);

        // 2. Xử lý sự kiện khi chọn Vai trò (Ẩn/Hiện ô nhập)
        rgRole.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_teacher) {
                    // Nếu là Giáo viên -> Ẩn mã SV và Lớp
                    tilStudentId.setVisibility(View.GONE);
                    tilClassId.setVisibility(View.GONE);
                } else {
                    // Nếu là Học sinh -> Hiện lại
                    tilStudentId.setVisibility(View.VISIBLE);
                    tilClassId.setVisibility(View.VISIBLE);
                }
            }
        });

        // Nút quay lại đăng nhập
        if (tvBackLogin != null) {
            tvBackLogin.setOnClickListener(v -> {
                Intent intent = new Intent(logup.this, login.class);
                startActivity(intent);
                finish();
            });
        }

        // 3. Xử lý khi bấm nút Đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy dữ liệu
                String name = etFullname.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String studentId = etStudentId.getText().toString().trim();
                String classId = etClassId.getText().toString().trim();

                // Xác định vai trò đang chọn
                String role = "student"; // Mặc định
                if (rgRole.getCheckedRadioButtonId() == R.id.rb_teacher) {
                    role = "teacher";
                }

                // --- KIỂM TRA DỮ LIỆU (VALIDATE) ---
                if (TextUtils.isEmpty(name)) {
                    etFullname.setError("Vui lòng nhập Họ tên"); return;
                }
                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Vui lòng nhập Email"); return;
                }
                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Vui lòng nhập Mật khẩu"); return;
                }
                if (password.length() < 6) {
                    etPassword.setError("Mật khẩu phải từ 6 ký tự trở lên"); return;
                }

                // CHỈ kiểm tra Mã SV nếu là Học sinh
                if (role.equals("student")) {
                    if (TextUtils.isEmpty(studentId)) {
                        etStudentId.setError("Vui lòng nhập Mã SV"); return;
                    }
                    // Có thể thêm kiểm tra lớp nếu cần
                }

                // Lưu biến role thành biến final để dùng trong hàm con (hoặc dùng biến cục bộ như dưới)
                String finalRole = role;

                // --- TẠO TÀI KHOẢN TRÊN FIREBASE ---
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userId = mAuth.getCurrentUser().getUid();

                                    // --- CHUẨN BỊ DỮ LIỆU LƯU FIRESTORE ---
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("fullName", name);
                                    user.put("email", email);
                                    user.put("role", finalRole); // Lưu đúng role (teacher/student)

                                    // Chỉ lưu mã SV và lớp nếu là Học sinh
                                    if (finalRole.equals("student")) {
                                        user.put("studentId", studentId);
                                        user.put("classId", classId);
                                    }

                                    // Lưu vào Collection "users"
                                    db.collection("users").document(userId)
                                            .set(user)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(logup.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(logup.this, login.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(logup.this, "Lỗi lưu dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(getApplicationContext(), "Lỗi đăng ký: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}