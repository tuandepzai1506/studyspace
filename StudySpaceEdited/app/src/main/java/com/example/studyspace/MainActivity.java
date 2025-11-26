package com.example.studyspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_act);
        ImageView add = (ImageView) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    // 1. Tải và chạy Animation
                    Animation flashAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.button_press_animation);
                    v.startAnimation(flashAnim);

                    // 2. Chuyển Activity sau khi Animation kết thúc
                    flashAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(), createQuizizz.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                };
            });
        CardView makeTest = (CardView) findViewById(R.id.makeTest);
        makeTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

            }
        });
    }  // <-- Chỉ còn 1 dấu đóng onCreate
}      // <-- đóng class



