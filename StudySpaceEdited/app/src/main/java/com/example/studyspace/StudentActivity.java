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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class StudentActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo Drawer và BottomNavigation
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        TextView username = findViewById(R.id.tv_username);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("fullName");
                            username.setText(name);
                        }
                    }
                });
        TextView MyEmail = findViewById(R.id.tv_email);
        String UserEmail = mAuth.getCurrentUser().getEmail();
        db.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String email = documentSnapshot.getString("email");
                    MyEmail.setText(email);
                }
            }
        });
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
                            Toast.makeText(StudentActivity.this, "Lỗi: Không tìm thấy ID drawer_layout trong XML!", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }else if (id == R.id.nav_class) {
                        Intent intent = new Intent(StudentActivity.this, showClass.class);
                        startActivity(intent);
                        return true;
                    }
                    else if (id == R.id.action_home){
                        Intent intent = new Intent(StudentActivity.this, StudentActivity.class);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            });
        } else {
            Toast.makeText(StudentActivity.this, "Lỗi: Không tìm thấy BottomNavigation!", Toast.LENGTH_SHORT).show();
        }
    }
    // Hàm phụ để xử lý animation và chuyển trang
    private void runAnimationAndSwitchActivity(android.view.View v, Class<?> destinationActivity) {
        try {
            Animation flashAnim = AnimationUtils.loadAnimation(StudentActivity.this, R.anim.button_press_animation);
            v.startAnimation(flashAnim);

            flashAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent intent = new Intent(StudentActivity.this, destinationActivity);
                    startActivity(intent);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        } catch (Exception e) {
            // Nếu lỗi animation thì chuyển trang luôn
            Intent intent = new Intent(StudentActivity.this, destinationActivity);
            startActivity(intent);
        }
    }
}