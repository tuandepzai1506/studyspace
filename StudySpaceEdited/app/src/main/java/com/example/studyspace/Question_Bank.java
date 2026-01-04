package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.QuestionAdapter;
import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class Question_Bank extends AppCompatActivity {

    private static final String TAG = "Question_Bank";

    private QuestionViewModel questionViewModel;
    private QuestionAdapter adapter;
    private com.google.android.material.textfield.TextInputEditText etFilterTopic;
    private android.widget.Spinner spinnerFilterLevel;
    private android.widget.ImageButton btnApplyFilter;
    private String selectedSubjectId;

    private final ActivityResultLauncher<Intent> addOrEditQuestionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Toast.makeText(this, "Cập nhật dữ liệu thành công.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_bank);

        selectedSubjectId = getIntent().getStringExtra("SELECTED_SUBJECT_ID");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // SỬA TẠI ĐÂY: Hiển thị tên môn học lên Toolbar nếu có
            String subjectName = getIntent().getStringExtra("SELECTED_SUBJECT_NAME");
            if (subjectName != null) getSupportActionBar().setTitle("Ngân hàng: " + subjectName);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new QuestionAdapter();
        recyclerView.setAdapter(adapter);

        questionViewModel.getQuestionsLiveData().observe(this, questions -> {
            if (questions != null) {
                adapter.setQuestions(questions);
            }
        });

        adapter.setOnItemClickListener(new QuestionAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Question question) {
                Intent intent = new Intent(Question_Bank.this, AddEditQuestionActivity.class);
                intent.putExtra("EXTRA_QUESTION_ID", question.getId());
                addOrEditQuestionLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Question question) {
                showDeleteConfirmationDialog(question);
            }
        });

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_question);
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(Question_Bank.this, AddEditQuestionActivity.class);
            // SỬA TẠI ĐÂY: Gửi ID môn học hiện tại để AddEditQuestionActivity tự gán vào Topic
            intent.putExtra("DEFAULT_TOPIC", selectedSubjectId);
            addOrEditQuestionLauncher.launch(intent);
        });

        etFilterTopic = findViewById(R.id.et_filter_topic);
        spinnerFilterLevel = findViewById(R.id.spinner_filter_level);
        btnApplyFilter = findViewById(R.id.btn_apply_filter);

        // SỬA TẠI ĐÂY: Thêm TextWatcher để lọc câu hỏi theo ký tự ngay khi gõ phím
        etFilterTopic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLocal(s.toString()); // Gọi hàm lọc cục bộ
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        String[] levels = {"Tất cả mức độ", "Mức 1", "Mức 2", "Mức 3", "Mức 4", "Mức 5"};
        android.widget.ArrayAdapter<String> levelAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterLevel.setAdapter(levelAdapter);

        // Nút lọc cũ (vẫn giữ lại để lọc theo mức độ nếu cần, nhưng gõ chữ sẽ ưu tiên lọc ký tự)
        btnApplyFilter.setOnClickListener(v -> {
            String topic = etFilterTopic.getText().toString().trim();
            int level = spinnerFilterLevel.getSelectedItemPosition();
            if (topic.isEmpty() && level == 0) {
                questionViewModel.startListening();
            } else {
                questionViewModel.stopListening();
                questionViewModel.filterQuestions(topic, level);
            }
        });
    }

    // SỬA TẠI ĐÂY: Hàm lọc cục bộ để tìm kiếm theo ký tự trong danh sách đã tải về
    private void filterLocal(String query) {
        List<Question> allQuestions = questionViewModel.getQuestionsLiveData().getValue();
        if (allQuestions == null) return;

        List<Question> filteredList = new ArrayList<>();
        for (Question q : allQuestions) {
            // Lọc không phân biệt hoa thường dựa trên nội dung câu hỏi
            if (q.getQuestionText().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(q);
            }
        }
        adapter.setQuestions(filteredList); // Cập nhật danh sách hiển thị
    }

    private void showDeleteConfirmationDialog(Question question) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa câu hỏi này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteQuestion(question))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteQuestion(Question question) {
        if (question.getId() == null) return;
        questionViewModel.deleteQuestion(question.getId())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa câu hỏi", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // SỬA TẠI ĐÂY: Logic load dữ liệu khi vào màn hình
        if (selectedSubjectId != null && !selectedSubjectId.isEmpty()) {
            questionViewModel.stopListening(); // Dừng lắng nghe toàn bộ để lọc theo môn học
            questionViewModel.filterQuestions(selectedSubjectId, 0); // Tải câu hỏi theo môn học đã chọn
        } else {
            questionViewModel.startListening(); // Load toàn bộ nếu không có ID môn học
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        questionViewModel.stopListening();
    }
}