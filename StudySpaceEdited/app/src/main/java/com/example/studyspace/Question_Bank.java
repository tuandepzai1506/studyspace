package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.QuestionAdapter;
import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Question_Bank extends AppCompatActivity {

    private static final String TAG = "Question_Bank";

    private QuestionViewModel questionViewModel;
    private QuestionAdapter adapter;

    // Launcher để hứng kết quả khi thêm/sửa câu hỏi xong
    private final ActivityResultLauncher<Intent> addOrEditQuestionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Toast.makeText(this, "Cập nhật dữ liệu thành công.", Toast.LENGTH_SHORT).show();
                    // Lưu ý: Dữ liệu sẽ tự động cập nhật nhờ LiveData và Firestore Listener
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_bank);

        // 1. Khởi tạo ViewModel
        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        // 2. Thiết lập RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new QuestionAdapter();
        recyclerView.setAdapter(adapter);

        // 3. Quan sát dữ liệu (LiveData)
        // Bất cứ khi nào Firestore thay đổi, hàm này sẽ chạy và cập nhật giao diện
        questionViewModel.getQuestionsLiveData().observe(this, questions -> {
            if (questions != null) {
                adapter.setQuestions(questions);
                // Hiển thị thông báo nếu danh sách rỗng (tùy chọn)
                if (questions.isEmpty()) {
                    // Log.d(TAG, "Danh sách câu hỏi trống");
                }
            }
        });

        // 4. Xử lý sự kiện click (Sửa/Xóa)
        adapter.setOnItemClickListener(new QuestionAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Question question) {
                // Mở màn hình sửa và truyền ID câu hỏi sang
                Intent intent = new Intent(Question_Bank.this, AddEditQuestionActivity.class);
                intent.putExtra("EXTRA_QUESTION_ID", question.getId()); // Gửi ID sang để bên kia biết là Sửa
                // Bạn cần sửa thêm AddEditQuestionActivity để nhận ID này và hiển thị dữ liệu cũ

                // Tạm thời hiển thị Toast
                Toast.makeText(Question_Bank.this, "Chức năng sửa đang hoàn thiện", Toast.LENGTH_SHORT).show();
                // addOrEditQuestionLauncher.launch(intent); // Bỏ comment khi đã code xong phần sửa
            }

            @Override
            public void onDeleteClick(Question question) {
                showDeleteConfirmationDialog(question);
            }
        });

        // 5. Nút thêm mới
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_question);
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(Question_Bank.this, AddEditQuestionActivity.class);
            addOrEditQuestionLauncher.launch(intent);
        });
    }

    // Hàm hiển thị hộp thoại xác nhận xóa
    private void showDeleteConfirmationDialog(Question question) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa câu hỏi này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteQuestion(question);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Gọi ViewModel để xóa
    private void deleteQuestion(Question question) {
        questionViewModel.deleteQuestion(question.getId())
                .addOnSuccessListener(aVoid -> Toast.makeText(Question_Bank.this, "Đã xóa câu hỏi", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Question_Bank.this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bắt đầu lắng nghe thời gian thực khi màn hình hiện lên
        questionViewModel.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dừng lắng nghe khi màn hình ẩn đi (để tiết kiệm pin/data)
        questionViewModel.stopListening();
    }
}