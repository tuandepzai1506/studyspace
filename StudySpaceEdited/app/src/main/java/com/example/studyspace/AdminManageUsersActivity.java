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

        // CẬP NHẬT: Khởi tạo Adapter với cả 2 sự kiện: Đổi quyền và Xóa
        adapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onChangeRole(User user) {
                showChangeRoleDialog(user);
            }

            @Override
            public void onDeleteUser(User user) {
                showDeleteConfirmDialog(user); // Gọi hàm xác nhận xóa
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadAllUsers();
    }

    // --- HÀM XÁC NHẬN XÓA ---
    private void showDeleteConfirmDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng " + user.getFullName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteUserFromFirestore(user);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- HÀM THỰC HIỆN XÓA TRÊN FIRESTORE ---
    private void deleteUserFromFirestore(User user) {
        db.collection("users").document(user.getUserId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    userList.remove(user); // Xóa khỏi danh sách hiển thị
                    adapter.notifyDataSetChanged(); // Cập nhật lại giao diện
                    Toast.makeText(this, "Đã xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadAllUsers() {
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
                .update("role", newRole)
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