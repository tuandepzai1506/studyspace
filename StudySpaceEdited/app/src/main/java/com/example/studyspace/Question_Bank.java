package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.QuestionAdapter;
import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Bỏ các comment không cần thiết và import dư thừa
import com.example.studyspace.R;

public class Question_Bank extends AppCompatActivity {

    private QuestionViewModel questionViewModel;
    private QuestionAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_bank);

        // --- Khởi tạo ViewModel ---
        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        // --- Thiết lập RecyclerView ---
        recyclerView = findViewById(R.id.recycler_view_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new QuestionAdapter();
        recyclerView.setAdapter(adapter);

        // --- Lắng nghe dữ liệu từ ViewModel ---
        questionViewModel.getAllQuestions().observe(this, questions -> {
            if (questions != null) {
                adapter.setQuestions(questions);
                if (questions.isEmpty()) {
                    Toast.makeText(this, "Chưa có câu hỏi nào trong ngân hàng.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Lỗi tải danh sách câu hỏi", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Xử lý sự kiện click trên các item của RecyclerView ---
        adapter.setOnItemClickListener(new QuestionAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Question question) {
                // TODO: Hoàn thiện chức năng sửa
                Toast.makeText(Question_Bank.this, "Chức năng sửa cho: " + question.getQuestionText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Question question) {
                // Hiển thị dialog xác nhận trước khi xóa
                new AlertDialog.Builder(Question_Bank.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa câu hỏi này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            questionViewModel.deleteQuestion(question.getId()).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Question_Bank.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                    // LiveData sẽ tự động cập nhật lại giao diện
                                } else {
                                    Toast.makeText(Question_Bank.this, "Xóa thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        // --- Xử lý nút thêm mới ---
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_question);
        fabAdd.setOnClickListener(view -> {
            // Mở màn hình AddEditQuestionActivity để thêm mới
            Intent intent = new Intent(Question_Bank.this, AddEditQuestionActivity.class);
            startActivity(intent);
        });
    }
}
