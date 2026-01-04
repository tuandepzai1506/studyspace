package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.SubjectAdapter;
import com.example.studyspace.models.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectSelectionActivity extends AppCompatActivity {
    private RecyclerView rvSubjects;
    private SubjectAdapter adapter;
    private List<Subject> subjectList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection);

        rvSubjects = findViewById(R.id.rv_subjects);
        rvSubjects.setLayoutManager(new GridLayoutManager(this, 2));

        // Ánh xạ FloatingActionButton và gán sự kiện
        FloatingActionButton fabAddSubject = findViewById(R.id.fab_add_subject);
        fabAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        adapter = new SubjectAdapter(subjectList, subject -> {
            // Khi nhấn vào môn học, chuyển sang Question_Bank và gửi ID môn đó đi
            Intent intent = new Intent(this, Question_Bank.class);
            intent.putExtra("SELECTED_SUBJECT_ID", subject.getId());
            intent.putExtra("SELECTED_SUBJECT_NAME", subject.getName());
            startActivity(intent);
        });

        rvSubjects.setAdapter(adapter);
        loadSubjects();
    }

    private void loadSubjects() {
        // Lấy danh sách đề mục từ collection "subjects"
        db.collection("subjects").get().addOnSuccessListener(queryDocumentSnapshots -> {
            subjectList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Subject s = doc.toObject(Subject.class);
                s.setId(doc.getId()); // Gán Document ID vào model
                subjectList.add(s);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void showAddSubjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_subject, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_subject_name);
        EditText etId = dialogView.findViewById(R.id.et_subject_id);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String id = etId.getText().toString().trim();

            if (!name.isEmpty() && !id.isEmpty()) {
                saveSubjectToFirestore(id, name);
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void saveSubjectToFirestore(String id, String name) {
        // Chuẩn bị dữ liệu Map để đẩy lên Firestore
        Map<String, Object> subject = new HashMap<>();
        subject.put("name", name);
        subject.put("icon", "ic_book"); // Mặc định sử dụng icon quyển sách

        // Lưu vào collection "subjects" với ID do Admin tự định nghĩa
        db.collection("subjects").document(id)
                .set(subject)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm đề mục: " + name, Toast.LENGTH_SHORT).show();
                    loadSubjects(); // Làm mới danh sách hiển thị
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}