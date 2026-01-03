package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button; // Đảm bảo đã import
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth; // Đảm bảo đã import
import androidx.cardview.widget.CardView;
public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // 1. Ánh xạ nút đăng xuất
        Button btnLogout = findViewById(R.id.btn_admin_logout);

        // 2. Đặt đoạn code xử lý sự kiện tại đây
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                // Đăng xuất khỏi Firebase
                FirebaseAuth.getInstance().signOut();

                // Chuyển hướng về màn hình đăng nhập
                Intent intent = new Intent(AdminDashboardActivity.this, login.class);

                // Cờ này giúp xóa hết các trang đã mở trước đó, ngăn Admin nhấn "Back" quay lại Dashboard
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish(); // Đóng trang Dashboard hiện tại
            });
        }

        // Các code xử lý CardView khác của bạn...
        // Trong AdminDashboardActivity.java
        CardView cardManageClasses = findViewById(R.id.card_manage_all_classes);
        cardManageClasses.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminManageClassActivity.class);
            startActivity(intent);
        });
        CardView cardManageUsers = findViewById(R.id.card_manage_users);
        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminManageUsersActivity.class);
            startActivity(intent);
        });
    }
}