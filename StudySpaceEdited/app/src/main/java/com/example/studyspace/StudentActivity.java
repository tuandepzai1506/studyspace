package com.example.studyspace;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference; // Import thêm
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue; // Quan trọng: Import để thêm vào mảng
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private CardView btnJoinClass, btnMyScore, btnShowClass;
    private TextView tvUsername, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadUserInfo();
        setupMainButtons();

        Button btn_logout = findViewById(R.id.btn_logout);
        if (btn_logout != null) {
            btn_logout.setOnClickListener(v -> {
                mAuth.signOut();
                runAnimationAndSwitchActivity(v, login.class);
            });
        }
        setupBottomNavigation();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        btnJoinClass = findViewById(R.id.joinClass);
        btnMyScore = findViewById(R.id.myScore);
        btnShowClass = findViewById(R.id.showClass);
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        if (tvUsername != null) tvUsername.setText(name);
                        if (tvEmail != null) tvEmail.setText(email);
                    }
                });
    }

    private void setupMainButtons() {
        if (btnJoinClass != null) {
            btnJoinClass.setOnClickListener(v -> showJoinClassDialog());
        }
        if (btnMyScore != null) {
            btnMyScore.setOnClickListener(v -> startActivity(new Intent(StudentActivity.this, scoreTable.class)));
        }
        if (btnShowClass != null) {
            btnShowClass.setOnClickListener(v -> {
                // Sửa dòng này để gọi Activity mới
                Intent intent = new Intent(StudentActivity.this, StudentClassListActivity.class);
                startActivity(intent);
            });        }
    }

    private void showJoinClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_join_class, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextInputEditText etClassCode = dialogView.findViewById(R.id.et_class_code);
        Button btnJoin = dialogView.findViewById(R.id.btn_join);

        btnJoin.setOnClickListener(v -> {
            String code = etClassCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                etClassCode.setError("Vui lòng nhập mã lớp!");
            } else {
                // Gọi hàm xử lý tham gia lớp
                joinClassInFirestore(code, dialog);
            }
        });

        dialog.show();
    }

    // --- HÀM XỬ LÝ LOGIC CHÍNH: THAM GIA LỚP VÀ CHUYỂN TRANG ---
    private void joinClassInFirestore(String classIdInput, AlertDialog dialog) {
        // Giả sử mã lớp học sinh nhập chính là Document ID của lớp học
        DocumentReference classRef = db.collection("classes").document(classIdInput);

        classRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 1. Tìm thấy lớp học
                String userId = mAuth.getCurrentUser().getUid();

                // 2. Thêm User ID vào danh sách thành viên (mảng 'member' hoặc 'members')
                // FieldValue.arrayUnion đảm bảo không bị trùng lặp
                classRef.update("member", FieldValue.arrayUnion(userId)) // Kiểm tra xem trên Firebase bạn đặt tên trường là "member" hay "members"
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(StudentActivity.this, "Tham gia thành công!", Toast.LENGTH_SHORT).show();

                            // 3. Lấy tên lớp để chuyển sang màn hình Chat
                            String className = documentSnapshot.getString("className");

                            // 4. Chuyển sang màn hình classroom
                            Intent intent = new Intent(StudentActivity.this, classroom.class);
                            intent.putExtra("classId", classIdInput);   // Gửi ID lớp
                            intent.putExtra("className", className);    // Gửi tên lớp
                            startActivity(intent);

                            dialog.dismiss(); // Đóng popup
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(StudentActivity.this, "Lỗi khi tham gia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } else {
                // Không tìm thấy lớp
                Toast.makeText(StudentActivity.this, "Mã lớp không tồn tại!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(StudentActivity.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.nav_profile) {
                        if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.END);
                        return true;
                    } else if (id == R.id.nav_class) {
                        startActivity(new Intent(StudentActivity.this, showClass.class));
                        return true;
                    } else if (id == R.id.action_home) {
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void runAnimationAndSwitchActivity(View v, Class<?> destinationActivity) {
        try {
            Animation flashAnim = AnimationUtils.loadAnimation(this, R.anim.button_press_animation);
            v.startAnimation(flashAnim);
            flashAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent intent = new Intent(StudentActivity.this, destinationActivity);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        } catch (Exception e) {
            startActivity(new Intent(this, destinationActivity));
            finish();
        }
    }
}