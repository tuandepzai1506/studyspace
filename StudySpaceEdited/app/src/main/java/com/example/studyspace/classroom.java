package com.example.studyspace;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class classroom extends AppCompatActivity {

    private ImageView imageBack;
    private TextView textName;
    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private FrameLayout layoutSendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classroom);

        initView();
        setListeners();
    }

    private void initView() {
        imageBack = findViewById(R.id.imageBack);
        textName = findViewById(R.id.textName);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        layoutSendBtn = findViewById(R.id.layoutSendBtn);

        // Giả lập tên người nhận
        textName.setText("Nguyễn Trọng Tuấn");
    }

    private void setListeners() {
        // 1. Xử lý nút quay lại
        imageBack.setOnClickListener(v -> onBackPressed());

        // 2. Xử lý nút gửi
        layoutSendBtn.setOnClickListener(v -> {
            String message = inputMessage.getText().toString();
            if (!message.isEmpty()) {
                // Code gửi tin nhắn sẽ viết ở đây
                Toast.makeText(classroom.this, "Đã gửi: " + message, Toast.LENGTH_SHORT).show();

                // Xóa ô nhập sau khi gửi
                inputMessage.setText("");
            }
        });
    }
}