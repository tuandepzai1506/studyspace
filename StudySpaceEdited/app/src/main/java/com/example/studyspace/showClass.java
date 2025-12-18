package com.example.studyspace;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.ClassAdapter;
import com.example.studyspace.ClassModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class showClass extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClassAdapter classAdapter;
    private List<ClassModel> mListClass;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_class);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ View
        recyclerView = findViewById(R.id.recycler_view_classes);
        tvEmpty = findViewById(R.id.tv_empty);

        // Cài đặt RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        mListClass = new ArrayList<>();
        classAdapter = new ClassAdapter(mListClass);
        recyclerView.setAdapter(classAdapter);

        // Gọi hàm lấy dữ liệu
        getListClassesFromFirebase();
    }

    private void getListClassesFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserId = currentUser.getUid();

        db.collection("classes")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ShowClass", "Lỗi lấy dữ liệu", error);
                        return;
                    }

                    if (value != null) {
                        mListClass.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            // Chuyển đổi Document thành Object ClassModel
                            ClassModel classModel = doc.toObject(ClassModel.class);
                            mListClass.add(classModel);
                        }

                        // Cập nhật giao diện
                        classAdapter.notifyDataSetChanged();

                        // Kiểm tra nếu danh sách trống thì hiện thông báo
                        if (mListClass.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                        }
                    }
                });
    }
}