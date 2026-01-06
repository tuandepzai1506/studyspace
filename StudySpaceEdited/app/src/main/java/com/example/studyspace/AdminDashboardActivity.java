package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings; // THÊM: Để lấy mã ID thiết bị
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // THÊM: Quản lý bộ lắng nghe

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // THÊM: Khai báo FirebaseAuth
    private ListenerRegistration adminSecurityListener; // THÊM: Bộ lắng nghe bảo mật

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // KHỞI TẠO

        // --- BẮT ĐẦU KIỂM TRA BẢO MẬT ---
        startSecurityCheck(); // THÊM: Gọi hàm kiểm tra thiết bị ngay khi mở Dashboard

        // --- ÁNH XẠ VIEW ---
        CardView cardUsers = findViewById(R.id.card_manage_users);
        CardView cardClasses = findViewById(R.id.card_manage_all_classes);
        CardView cardStats = findViewById(R.id.card_statistics);
        CardView cardSettings = findViewById(R.id.card_settings);
        Button btnLogout = findViewById(R.id.btn_admin_logout);

        // --- THIẾT LẬP SỰ KIỆN CLICK ---

        // 1. Quản lý Người dùng
        cardUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminManageUsersActivity.class));
        });

        // 2. Quản lý Lớp học
        cardClasses.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminManageClassActivity.class));
        });

        // 3. Thống kê
        cardStats.setOnClickListener(v -> {
            showQuickStats();
        });

        // 4. Cài đặt
        cardSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // 5. Đăng xuất
        btnLogout.setOnClickListener(v -> {
            if (adminSecurityListener != null) adminSecurityListener.remove(); // Gỡ bộ lắng nghe khi chủ động logout
            mAuth.signOut();
            Intent intent = new Intent(this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // THÊM: Hàm kiểm tra thiết bị đăng nhập thời gian thực
    private void startSecurityCheck() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            String myDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            adminSecurityListener = db.collection("users").document(userId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) return;

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String serverDeviceId = documentSnapshot.getString("currentDeviceId");

                            // Nếu ID trên Server không khớp với ID máy này -> Đá phiên đăng nhập cũ
                            if (serverDeviceId != null && !serverDeviceId.equals(myDeviceId)) {
                                showForceLogoutDialog();
                            }
                        }
                    });
        }
    }

    // THÊM: Hiển thị Dialog thông báo khi bị đăng nhập nơi khác
    private void showForceLogoutDialog() {
        if (isFinishing()) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cảnh báo bảo mật")
                .setMessage("Tài khoản Admin đã được đăng nhập ở thiết bị khác. Phiên làm việc này sẽ kết thúc để đảm bảo an toàn.")
                .setCancelable(false)
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    // Hàm hiển thị thống kê nhanh từ Firestore
    private void showQuickStats() {
        db.collection("users").get().addOnSuccessListener(users -> {
            int userCount = users.size();
            db.collection("classes").get().addOnSuccessListener(classes -> {
                int classCount = classes.size();
                String message = "Tổng số người dùng: " + userCount + "\nTổng số lớp học: " + classCount;

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Thống Kê Hệ Thống")
                        .setMessage(message)
                        .setPositiveButton("Đóng", null)
                        .show();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // THÊM: Giải phóng bộ lắng nghe để tránh rò rỉ bộ nhớ
        if (adminSecurityListener != null) {
            adminSecurityListener.remove();
        }
    }
}