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

    // SỬA LỖI: Chỉ khai báo các biến một lần duy nhất
    private QuestionViewModel questionViewModel;
    private QuestionAdapter adapter;

    // Cơ chế mới để xử lý kết quả trả về từ AddEditQuestionActivity
    private final ActivityResultLauncher<Intent> addOrEditQuestionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Kiểm tra xem Activity con có trả về kết quả là "OK" không
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    // Dữ liệu sẽ được SnapshotListener tự động cập nhật,
                    // chúng ta chỉ cần hiển thị thông báo nếu muốn.
                    Log.d(TAG, "Nhận được kết quả OK. Dữ liệu sẽ được cập nhật tự động.");
                    Toast.makeText(this, "Danh sách đã được cập nhật.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // SỬA LỖI: Chỉ giữ lại một phương thức onCreate duy nhất và hoàn chỉnh
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_bank);

        // --- Khởi tạo ViewModel ---
        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        // --- Thiết lập RecyclerView ---
        RecyclerView recyclerView = findViewById(R.id.recycler_view_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new QuestionAdapter();
        recyclerView.setAdapter(adapter);

        // --- Lắng nghe LiveData từ ViewModel ---
        // Giao diện sẽ tự động cập nhật mỗi khi dữ liệu trong LiveData thay đổi
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

        // --- Xử lý sự kiện click trên các item ---
        adapter.setOnItemClickListener(new QuestionAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Question question) {
                // TODO: Triển khai chức năng sửa trong tương lai
                // Intent intent = new Intent(Question_Bank.this, AddEditQuestionActivity.class);
                // intent.putExtra("EDIT_QUESTION_ID", question.getId());
                // addOrEditQuestionLauncher.launch(intent);
                Toast.makeText(Question_Bank.this, "Chức năng sửa chưa được triển khai", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Question question) {
                new AlertDialog.Builder(Question_Bank.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa câu hỏi này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // Chỉ cần gọi delete, SnapshotListener sẽ tự động cập nhật UI
                            questionViewModel.deleteQuestion(question.getId())
                                    .addOnSuccessListener(aVoid -> Toast.makeText(Question_Bank.this, "Xóa thành công", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(Question_Bank.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        // --- Xử lý nút thêm mới ---
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_question);
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(Question_Bank.this, AddEditQuestionActivity.class);
            // Sử dụng Launcher mới để mở Activity
            addOrEditQuestionLauncher.launch(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Khi Activity bắt đầu được hiển thị, hãy bắt đầu lắng nghe thay đổi từ Firestore
        Log.d(TAG, "Bắt đầu lắng nghe thay đổi...");
        questionViewModel.startListeningForQuestionChanges();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Khi Activity không còn hiển thị, dừng lắng nghe để tiết kiệm tài nguyên
        Log.d(TAG, "Dừng lắng nghe thay đổi.");
        questionViewModel.stopListeningForChanges();
    }
}
