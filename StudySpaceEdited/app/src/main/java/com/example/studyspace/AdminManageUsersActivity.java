package com.example.studyspace;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.adapters.UserAdapter;
import com.example.studyspace.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.rv_all_users);
        userList = new ArrayList<>();

        // Khởi tạo Adapter với sự kiện đổi quyền (Role)
        adapter = new UserAdapter(userList, user -> showChangeRoleDialog(user));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadAllUsers();
    }

    private void loadAllUsers() {
        // Lấy tất cả người dùng trong hệ thống
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            userList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                User user = doc.toObject(User.class);
                user.setUserId(doc.getId());
                userList.add(user);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi tải người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void showChangeRoleDialog(User user) {
        String[] roles = {"student", "teacher", "admin"};
        new AlertDialog.Builder(this)
                .setTitle("Thay đổi vai trò: " + user.getFullName())
                .setItems(roles, (dialog, which) -> {
                    String selectedRole = roles[which];
                    updateUserRole(user, selectedRole);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateUserRole(User user, String newRole) {
        db.collection("users").document(user.getUserId())
                .update("role", newRole) // Cập nhật role mới lên Firestore
                .addOnSuccessListener(aVoid -> {
                    user.setRole(newRole);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã cập nhật thành " + newRole, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}