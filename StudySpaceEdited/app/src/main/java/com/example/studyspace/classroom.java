package com.example.studyspace;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.ChatAdapter;
import com.example.studyspace.models.ChatMessage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class classroom extends AppCompatActivity {

    // Khai báo biến View
    private ImageView imageBack;
    private TextView textName;
    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private FrameLayout layoutSendBtn;

    // Khai báo biến Logic
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String classId; // ID của lớp học hiện tại
    private String className;

    // Adapter và List tin nhắn
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classroom);

        // 1. Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 2. Ánh xạ View
        initView();

        // 3. Nhận dữ liệu từ showClass chuyển sang
        loadReceiverDetails();

        // 4. Cài đặt RecyclerView (Danh sách tin nhắn)
        setupRecyclerView();

        // 5. Lắng nghe tin nhắn từ Firebase
        listenMessages();

        // 6. Cài đặt sự kiện click
        setListeners();
    }

    private void initView() {
        imageBack = findViewById(R.id.imageBack);
        textName = findViewById(R.id.textName);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        layoutSendBtn = findViewById(R.id.layoutSendBtn);
    }

    // Hàm nhận dữ liệu từ Intent
    private void loadReceiverDetails() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            classId = bundle.getString("classId");
            className = bundle.getString("className");

            // Hiển thị tên lớp lên tiêu đề
            textName.setText(className);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID lớp học", Toast.LENGTH_SHORT).show();
            finish(); // Đóng màn hình nếu không có ID
        }
    }

    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        // Bạn cần tạo ChatAdapter.java để dòng này hoạt động
        chatAdapter = new ChatAdapter(chatMessages, mAuth.getCurrentUser().getUid(), classId);

        chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Luôn hiển thị tin nhắn mới nhất ở cuối
        chatRecyclerView.setLayoutManager(layoutManager);
    }

    private void setListeners() {
        // Nút quay lại
        imageBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Nút gửi tin nhắn
        layoutSendBtn.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Tạo dữ liệu tin nhắn để gửi lên Firebase
        Map<String, Object> messageObj = new HashMap<>();
        messageObj.put("senderId", mAuth.getCurrentUser().getUid());
        messageObj.put("message", messageText);
        messageObj.put("timestamp", new Date());

        // Lưu vào đường dẫn: classes -> [ID lớp] -> messages -> [ID tin nhắn tự sinh]
        if (classId != null) {
            db.collection("classes").document(classId).collection("messages")
                    .add(messageObj)
                    .addOnSuccessListener(documentReference -> {
                        // Gửi thành công thì xóa ô nhập
                        inputMessage.setText(null);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(classroom.this, "Gửi thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void listenMessages() {
        if (classId == null) return;
        db.collection("classes").document(classId).collection("messages")
                .orderBy("timestamp") // Sắp xếp theo thời gian
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        if (value != null) {
                            int count = chatMessages.size();
                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                    // Convert dữ liệu Firebase thành Object ChatMessage
                                    ChatMessage chatMessage = documentChange.getDocument().toObject(ChatMessage.class);
                                    chatMessages.add(chatMessage);
                                }
                            }

                            if (count == 0) {
                                chatAdapter.notifyDataSetChanged();
                            } else {
                                // Chỉ cập nhật những tin nhắn mới thêm vào để mượt hơn
                                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                                chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                            }

                            // Cuộn xuống tin nhắn cuối cùng
                            if (chatMessages.size() > 0) {
                                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                            }
                        }
                    }
                });
    }
}