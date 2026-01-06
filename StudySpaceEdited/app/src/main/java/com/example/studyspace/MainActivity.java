package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings; // THÊM: Để lấy mã ID thiết bị
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
import com.google.firebase.firestore.ListenerRegistration; // THÊM: Quản lý bộ lắng nghe

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView username, MyEmail;
    private ListenerRegistration userListener; // THÊM: Khai báo bộ lắng nghe để giải phóng khi cần

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

            // CẬP NHẬT: Lấy mã ID của thiết bị hiện tại đang cầm trên tay
            String myDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            // CẬP NHẬT: Thay đổi từ .get() sang .addSnapshotListener để lắng nghe thay đổi thời gian thực
            userListener = db.collection("users").document(userId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            android.util.Log.e("AUTH_CHECK", "Lỗi lắng nghe: " + e.getMessage());
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // BƯỚC QUAN TRỌNG: Kiểm tra mã thiết bị trên Server
                            String serverDeviceId = documentSnapshot.getString("currentDeviceId");

                            // KIỂM TRA: Nếu mã trên Server khác mã máy này -> Có người khác vừa đăng nhập ở máy mới
                            if (serverDeviceId != null && !serverDeviceId.equals(myDeviceId)) {
                                showForceLogoutDialog();
                                return; // Dừng xử lý các lệnh bên dưới
                            }

                            String role = documentSnapshot.getString("role");
                            android.util.Log.d("ADMIN_CHECK", "Role tren Server la: " + role);

                            if ("admin".equals(role)) {
                                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                username.setText(documentSnapshot.getString("fullName"));
                                MyEmail.setText(documentSnapshot.getString("email"));
                            }
                        } else {
                            // HIỂN THỊ THÔNG TIN CHI TIẾT ĐỂ ĐỐI CHIẾU
                            String errorInfo = "ID dang tim: " + userId + "\nEmail: " + mAuth.getCurrentUser().getEmail();
                            username.setText("KHÔNG TÌM THẤY TRÊN DATABASE!");

                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Thông tin định danh")
                                    .setMessage(errorInfo)
                                    .setPositiveButton("Đã hiểu", null)
                                    .show();
                        }
                    });
        }
    }

    // THÊM: Hàm hiển thị thông báo và cưỡng ép đăng xuất
    private void showForceLogoutDialog() {
        if (isFinishing()) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cảnh báo bảo mật")
                .setMessage("Tài khoản của bạn vừa được đăng nhập ở một thiết bị khác. Bạn sẽ bị đăng xuất khỏi thiết bị này.")
                .setCancelable(false) // Không cho phép nhấn ra ngoài để bỏ qua
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    if (userListener != null) userListener.remove(); // Gỡ bộ lắng nghe
                    mAuth.signOut(); // Đăng xuất Firebase
                    Intent intent = new Intent(MainActivity.this, login.class);
                    // Xóa sạch lịch sử các màn hình trước đó
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // THÊM: Giải phóng bộ lắng nghe khi hủy Activity để tránh rò rỉ bộ nhớ
        if (userListener != null) userListener.remove();
    }
}