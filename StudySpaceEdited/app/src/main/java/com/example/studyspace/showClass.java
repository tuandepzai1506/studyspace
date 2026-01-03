package com.example.studyspace;

import android.content.Intent; // Nhớ import Intent
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        // Setup Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar_show_class);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

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

        // --- PHẦN THAY ĐỔI QUAN TRỌNG Ở ĐÂY ---
        // Khởi tạo Adapter với Interface lắng nghe sự kiện click
        // Khởi tạo Adapter với Interface lắng nghe sự kiện click
        // Trong onCreate của showClass.java
        classAdapter = new ClassAdapter(mListClass, new ClassAdapter.IClickItemClassListener() {
            @Override
            public void onClickItemClass(ClassModel classModel) {
                goToChatScreen(classModel);
            }

            @Override
            public void onDeleteClick(ClassModel classModel, int position) {
                // Gọi hàm xác nhận xóa
                confirmDeleteClass(classModel, position);
            }
        });

        recyclerView.setAdapter(classAdapter);

        // Gọi hàm lấy dữ liệu
        getListClassesFromFirebase();
    }

    // Hàm chuyển màn hình sang ChatActivity
    private void goToChatScreen(ClassModel classModel) {
        // Kiểm tra xem lớp học có hợp lệ không
        if (classModel == null || classModel.getClassId() == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID lớp học", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Intent để chuyển màn hình
        // Lưu ý: Bạn cần phải tạo file ChatActivity.java trước thì dòng dưới mới hết báo đỏ
        Intent intent = new Intent(showClass.this, classroom.class);

        // Đóng gói dữ liệu (ID lớp, Tên lớp) để gửi sang màn hình Chat
        Bundle bundle = new Bundle();
        bundle.putString("classId", classModel.getClassId());
        bundle.putString("className", classModel.getClassName());

        intent.putExtras(bundle);

        // Bắt đầu chuyển màn hình
        startActivity(intent);
    }

    private void getListClassesFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return; // Kiểm tra null để tránh lỗi crash

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

                            // Đảm bảo lấy đúng ID của document gán vào model (nếu cần)
                            classModel.setClassId(doc.getId());

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
    private void confirmDeleteClass(ClassModel classModel, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa lớp học")
                .setMessage("Bạn có chắc chắn muốn xóa lớp '" + classModel.getClassName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Thực hiện xóa trên Firebase Firestore
                    db.collection("classes").document(classModel.getClassId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Xóa thành công trên Server, tiến hành cập nhật UI cục bộ
                                // Lưu ý: Nếu bạn dùng addSnapshotListener thì Firebase sẽ tự
                                // cập nhật mListClass, nhưng gọi lệnh dưới đây sẽ giúp UI mượt hơn ngay lập tức
                                Toast.makeText(showClass.this, "Đã xóa lớp thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(showClass.this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}