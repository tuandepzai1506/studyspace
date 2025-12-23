package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.ClassListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StudentClassListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClassListAdapter adapter; // Dùng Adapter mới
    private List<ClassModel> mListClasses;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_class_list); // XML vừa tạo ở 4.1

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.rv_student_classes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mListClasses = new ArrayList<>();

        // Khởi tạo Adapter mới với Listener
        adapter = new ClassListAdapter(mListClasses, new ClassListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ClassModel classModel) {
                // Chuyển sang màn hình Chat
                Intent intent = new Intent(StudentClassListActivity.this, classroom.class);
                intent.putExtra("classId", classModel.getClassId());
                intent.putExtra("className", classModel.getClassName());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        loadDataFromFirebase();
    }

    private void loadDataFromFirebase() {
        if (mAuth.getCurrentUser() == null) return;
        String myUserId = mAuth.getCurrentUser().getUid();

        // Tìm lớp mà mảng "member" có chứa ID của tôi
        db.collection("classes")
                .whereArrayContains("member", myUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mListClasses.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ClassModel model = doc.toObject(ClassModel.class);
                        if (model != null) {
                            model.setClassId(doc.getId()); // Đảm bảo lấy đúng ID
                            mListClasses.add(model);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (mListClasses.isEmpty()) {
                        Toast.makeText(this, "Bạn chưa tham gia lớp nào", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}