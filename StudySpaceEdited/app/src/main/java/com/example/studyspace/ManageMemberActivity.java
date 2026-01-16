package com.example.studyspace;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.adapters.MemberAdapter;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.studyspace.R;
import android.util.Log;

public class ManageMemberActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private List<Map<String, String>> studentDataList;
    private String classId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_member);

        db = FirebaseFirestore.getInstance();
        classId = getIntent().getStringExtra("classId");

        recyclerView = findViewById(R.id.rv_members);
        studentDataList = new ArrayList<>();

        adapter = new MemberAdapter(studentDataList, (studentId, studentName, position) -> {
            confirmDelete(studentId, studentName, position);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadMembersFromClass();
    }

    private void loadMembersFromClass() {
        db.collection("classes").document(classId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 1. Lấy UID của giáo viên (người tạo lớp)
                String teacherId = documentSnapshot.getString("userId");

                // 2. Lấy danh sách tất cả thành viên
                List<String> memberUids = (List<String>) documentSnapshot.get("member");

                if (memberUids != null) {
                    studentDataList.clear(); // Xóa list cũ trước khi nạp mới
                    for (String uid : memberUids) {
                        // 3. SO SÁNH: Nếu UID thành viên KHÁC với UID giáo viên thì mới hiển thị
                        if (teacherId != null && !uid.equals(teacherId)) {
                            fetchStudentInfo(uid);
                        }
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu lớp", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchStudentInfo(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                Map<String, String> student = new HashMap<>();
                student.put("uid", uid);

                // 1. Lấy dữ liệu từ Firestore
                String fullName = userDoc.getString("fullName");
                String sId = userDoc.getString("studentId");

                // 2. Kiểm tra null và nối chuỗi
                String nameDisplay = (fullName != null ? fullName : "N/A");
                String idDisplay = (sId != null ? sId : "N/A");

                // 3. Đưa chuỗi đã nối vào Map với key là "name" (để Adapter hiển thị)
                student.put("name", nameDisplay + " (" + idDisplay + ")");

                studentDataList.add(student);
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            Log.e("ManageMember", "Lỗi tải sinh viên: " + e.getMessage());
        });
    }

    private void confirmDelete(String studentId, String studentName, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thành viên")
                .setMessage("Bạn có chắc muốn mời " + studentName + " ra khỏi lớp?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("classes").document(classId)
                            .update("member", FieldValue.arrayRemove(studentId))
                            .addOnSuccessListener(aVoid -> {
                                studentDataList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Đã xóa thành viên", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}