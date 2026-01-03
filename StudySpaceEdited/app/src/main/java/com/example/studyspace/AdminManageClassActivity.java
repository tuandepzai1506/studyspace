package com.example.studyspace;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.adapters.ClassAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import com.example.studyspace.adapters.ClassAdapter;
public class AdminManageClassActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private List<ClassModel> classList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_all_classes); // Bạn cần tạo file XML này

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.rv_all_classes);
        classList = new ArrayList<>();

        // Tận dụng lại ClassAdapter bạn đã có
        adapter = new ClassAdapter(classList, new ClassAdapter.IClickItemClassListener() {
            @Override
            public void onClickItemClass(ClassModel classModel) {
                // Admin có thể click để vào xem nội dung lớp
            }

            @Override
            public void onDeleteClick(ClassModel classModel, int position) {
                confirmDeleteClass(classModel, position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadAllClasses();
    }

    private void loadAllClasses() {
        // KHÁC BIỆT: Lấy toàn bộ collection, không lọc theo userId
        db.collection("classes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            classList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                ClassModel model = doc.toObject(ClassModel.class);
                model.setClassId(doc.getId());
                classList.add(model);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void confirmDeleteClass(ClassModel classModel, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Cảnh báo Admin")
                .setMessage("Bạn có chắc chắn muốn xóa lớp '" + classModel.getClassName() + "' của giáo viên khác không?")
                .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                    db.collection("classes").document(classModel.getClassId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                classList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Admin đã xóa lớp thành công", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}