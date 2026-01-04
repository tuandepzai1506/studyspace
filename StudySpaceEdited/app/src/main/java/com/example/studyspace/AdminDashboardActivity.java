package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

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

        // 3. Thống kê (Hiển thị nhanh tổng số lượng qua Toast hoặc Dialog)
        cardStats.setOnClickListener(v -> {
            showQuickStats();
        });

        // 4. Cài đặt (Bạn có thể thêm activity cài đặt sau)
        cardSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // 5. Đăng xuất
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
}