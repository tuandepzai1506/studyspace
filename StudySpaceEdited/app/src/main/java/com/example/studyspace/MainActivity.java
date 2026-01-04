package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView username, MyEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_act);

        // 1. Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 2. Ánh xạ View
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        username = findViewById(R.id.tv_username);
        MyEmail = findViewById(R.id.tv_email);

        // 3. Kiểm tra User và Role (Admin/Student)
        checkUserRole();

        // 4. Thiết lập sự kiện cho các nút
        setupButtons();

        // 5. Thiết lập Menu Bottom
        setupBottomNavigation();
    }

    private void checkUserRole() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // ÉP BUỘC LẤY DỮ LIỆU TỪ SERVER (Không dùng cache)
            db.collection("users").document(userId).get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            // In ra Logcat để kiểm tra chính xác giá trị role nhận được
                            android.util.Log.d("ADMIN_CHECK", "Role tren Server la: " + role);

                            if ("admin".equals(role)) {
                                    Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                    return;
                            } else {
                                username.setText(documentSnapshot.getString("fullName"));
                                MyEmail.setText(documentSnapshot.getString("email"));
                            }
                        } else {
                            // HIỂN THỊ THÔNG TIN CHI TIẾT ĐỂ ĐỐI CHIẾU
                            String errorInfo = "ID dang tim: " + userId + "\nEmail: " + mAuth.getCurrentUser().getEmail();
                            username.setText("KHÔNG TÌM THẤY TRÊN DATABASE!");

                            // Hiển thị Dialog chứa ID để bạn copy
                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Thông tin định danh")
                                    .setMessage(errorInfo)
                                    .setPositiveButton("Đã hiểu", null)
                                    .show();

                            android.util.Log.e("ADMIN_CHECK", errorInfo);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi kết nối Server: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void setupButtons() {
        // Nút Tạo lớp (ImageView)
        ImageView createClass = findViewById(R.id.classCreate);
        if (createClass != null) createClass.setOnClickListener(v -> runAnimationAndSwitchActivity(v, createClass.class));

        // Nút Tạo bộ đề (CardView)
        CardView makeTest = findViewById(R.id.makeTest);
        if (makeTest != null) makeTest.setOnClickListener(v -> runAnimationAndSwitchActivity(v, TaoBoDe.class));

        // Nút Ngân hàng câu hỏi (CardView)
        CardView qs_bank = findViewById(R.id.button_question_bank);
        if (qs_bank != null) qs_bank.setOnClickListener(v -> runAnimationAndSwitchActivity(v, SubjectSelectionActivity.class));

        // Nút Xem lớp (CardView)
        CardView showClass = findViewById(R.id.showClass);
        if (showClass != null) showClass.setOnClickListener(v -> runAnimationAndSwitchActivity(v, showClass.class));

        // Nút Bảng điểm (CardView)
        CardView bangDiem = findViewById(R.id.bangDiem);
        if (bangDiem != null) bangDiem.setOnClickListener(v -> runAnimationAndSwitchActivity(v, ClassScoresActivity.class));

        // Nút Đăng xuất
        Button btn_logout = findViewById(R.id.btn_logout);
        if (btn_logout != null) btn_logout.setOnClickListener(v -> runAnimationAndSwitchActivity(v, login.class));
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_profile) {
                    if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.END);
                } else if (id == R.id.nav_class) {
                    startActivity(new Intent(MainActivity.this, showClass.class));
                } else if (id == R.id.action_home) {
                    // Đang ở Home rồi không cần làm gì
                }
                return true;
            });
        }
    }

    private void runAnimationAndSwitchActivity(android.view.View v, Class<?> destinationActivity) {
        try {
            Animation flashAnim = AnimationUtils.loadAnimation(this, R.anim.button_press_animation);
            v.startAnimation(flashAnim);
            flashAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    startActivity(new Intent(MainActivity.this, destinationActivity));
                }
            });
        } catch (Exception e) {
            startActivity(new Intent(MainActivity.this, destinationActivity));
        }
    }
}