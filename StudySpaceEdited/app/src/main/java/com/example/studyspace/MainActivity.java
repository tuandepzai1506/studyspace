package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    // Khai báo biến toàn cục để quản lý dễ hơn
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_act);
        // Khởi tạo Drawer và BottomNavigation
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Nút Add (ImageView)
        ImageView add = findViewById(R.id.add);
        if (add != null) {
            add.setOnClickListener(v -> {
                runAnimationAndSwitchActivity(v, createQuizizz.class);
            });
        }
        // Nút Tạo bộ đề (CardView)
        CardView makeTest = findViewById(R.id.makeTest);
        if (makeTest != null) {
            makeTest.setOnClickListener(v -> {
                runAnimationAndSwitchActivity(v, TaoBoDe.class);
            });
        }
        // Nút Ngân hàng câu hỏi (CardView)
        CardView qs_bank = findViewById(R.id.button_question_bank);
        if (qs_bank != null){
            qs_bank.setOnClickListener(v ->{
                runAnimationAndSwitchActivity(v, Question_Bank.class);
            });
        }

        // Nút Tạo lớp (CardView)
        CardView classCreate = findViewById(R.id.classCreate);
        if (classCreate != null){
            classCreate.setOnClickListener(v ->{
                runAnimationAndSwitchActivity(v, createClass.class);
            });
        }

        // Nút Bảng điểm (CardView)
        CardView bangDiem = findViewById(R.id.bangDiem);
        if (bangDiem != null){
            bangDiem.setOnClickListener(v ->{
                runAnimationAndSwitchActivity(v, scoreTable.class);
            });
        }
        Button btn_logout = findViewById(R.id.btn_logout);
        if (btn_logout != null) {
            btn_logout.setOnClickListener(v -> {
                    runAnimationAndSwitchActivity(v, login.class);
            });
        }


        // --- XỬ LÝ MENU BOTTOM ---
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.nav_profile) {
                        if (drawerLayout != null) {
                            drawerLayout.openDrawer(GravityCompat.END);
                            return true;
                        } else {
                            Toast.makeText(MainActivity.this, "Lỗi: Không tìm thấy ID drawer_layout trong XML!", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    } else if (id == R.id.nav_quizizz) {
                        Intent intent = new Intent(MainActivity.this, createQuizizz.class);
                        startActivity(intent);
                        return true;
                    }else if (id == R.id.nav_class) {
                        Intent intent = new Intent(MainActivity.this, classroom.class);
                        startActivity(intent);
                        return true;
                    }
                    else if (id == R.id.nav_home){
                        Intent intent = new Intent (MainActivity.this, MainActivity.class);
                    }
                    return false;
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Lỗi: Không tìm thấy BottomNavigation!", Toast.LENGTH_SHORT).show();
        }
    }
    // Hàm phụ để xử lý animation và chuyển trang
    private void runAnimationAndSwitchActivity(android.view.View v, Class<?> destinationActivity) {
        try {
            Animation flashAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.button_press_animation);
            v.startAnimation(flashAnim);

            flashAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent intent = new Intent(MainActivity.this, destinationActivity);
                    startActivity(intent);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        } catch (Exception e) {
            // Nếu lỗi animation thì chuyển trang luôn
            Intent intent = new Intent(MainActivity.this, destinationActivity);
            startActivity(intent);
        }
    }
}