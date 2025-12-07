package com.example.studyspace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // Bắt buộc phải có dòng này

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class createQuizizz extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_quizizz);

        // Code chỉnh giao diện hệ thống (Giữ nguyên)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- BẮT ĐẦU PHẦN KÍCH HOẠT NÚT BẤM ---

        // 1. Tìm cái nút trong giao diện (Phải khai báo là Button)
        Button btnQuizizz = findViewById(R.id.quizizz);

        // 2. Cài đặt hành động: Khi bấm vào thì làm gì?
        if (btnQuizizz != null) {
            btnQuizizz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Đường dẫn bạn muốn mở
                    String url = "https://wayground.com/admin?newUser=true";

                    // Lệnh mở trình duyệt web (Chrome, Cốc Cốc...)
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });
        }
    }
}