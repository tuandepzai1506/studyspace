package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast; // Thêm Toast để thông báo lỗi nếu có

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_act); // Đảm bảo tên file layout này đúng

        // 1. Xử lý nút Ngân hàng câu hỏi
        CardView buttonQuestionBank = findViewById(R.id.button_question_bank);
        if (buttonQuestionBank != null) {
            buttonQuestionBank.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, QuestionManagementActivity.class);
                startActivity(intent);
            });
        }

        // 2. Xử lý nút "add" (ImageView)
        ImageView add = findViewById(R.id.add);
        if (add != null) {
            add.setOnClickListener(v -> {
                try {
                    Animation flashAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.button_press_animation);
                    v.startAnimation(flashAnim);

                    flashAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Intent intent = new Intent(MainActivity.this, createQuizizz.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                } catch (Exception e) {
                    // Nếu thiếu file animation thì vẫn chuyển trang được
                    Intent intent = new Intent(MainActivity.this, createQuizizz.class);
                    startActivity(intent);
                }
            });
        } else {
            // Log lỗi nếu không tìm thấy ID
            System.out.println("Lỗi: Không tìm thấy ID 'add' trong layout");
        }

        // 3. Xử lý nút "makeTest" (CardView)
        CardView makeTest = findViewById(R.id.makeTest);
        if (makeTest != null) {
            makeTest.setOnClickListener(v -> {
                try {
                    Animation flashAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.button_press_animation);
                    v.startAnimation(flashAnim);

                    flashAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Intent intent = new Intent(MainActivity.this, TaoBoDe.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                } catch (Exception e) {
                    Intent intent = new Intent(MainActivity.this, TaoBoDe.class);
                    startActivity(intent);
                }
            });
        }
    }
}