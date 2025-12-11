package com.example.studyspace;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddEditQuestionActivity extends AppCompatActivity {

    private QuestionViewModel questionViewModel;
    private static final String TAG = "AddEditActivity";

    private TextInputEditText etQuestionText, etOption1, etOption2, etOption3, etOption4, etTopic;
    private Spinner spinnerLevel;
    private Button btnSave;

    // TODO: Thêm biến để xử lý chế độ Sửa
    // private String currentQuestionId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_question);

        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        // Ánh xạ các view từ layout
        etQuestionText = findViewById(R.id.edit_text_question_text);
        etOption1 = findViewById(R.id.edit_text_option1);
        etOption2 = findViewById(R.id.edit_text_option2);
        etOption3 = findViewById(R.id.edit_text_option3);
        etOption4 = findViewById(R.id.edit_text_option4);
        etTopic = findViewById(R.id.edit_text_topic);
        spinnerLevel = findViewById(R.id.spinner_level);
        btnSave = findViewById(R.id.button_save);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_edit);

        // Thiết lập Spinner cho độ khó
        setupLevelSpinner();

        // Xử lý nút back trên toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Xử lý nút Lưu
        btnSave.setOnClickListener(v -> saveQuestion());
    }

    private void setupLevelSpinner() {
        // Tạo dữ liệu cho Spinner
        Integer[] levels = new Integer[]{1, 2, 3, 4, 5};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(adapter);
    }

    private void saveQuestion() {
        String questionText = etQuestionText.getText().toString().trim();
        String option1 = etOption1.getText().toString().trim();
        String option2 = etOption2.getText().toString().trim();
        String option3 = etOption3.getText().toString().trim();
        String option4 = etOption4.getText().toString().trim();
        String topic = etTopic.getText().toString().trim();
        int level = (Integer) spinnerLevel.getSelectedItem();

        Log.d(TAG, "== BẮT ĐẦU LƯU ==");
        Log.d(TAG, "Nội dung: " + questionText);
        Log.d(TAG, "Đáp án 1: " + option1);
        Log.d(TAG, "Chủ đề: " + topic);

        // Kiểm tra dữ liệu đầu vào
        if (TextUtils.isEmpty(questionText) || TextUtils.isEmpty(option1) || TextUtils.isEmpty(option2) || TextUtils.isEmpty(topic)) {
            Toast.makeText(this, "Vui lòng nhập đủ các trường bắt buộc", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lỗi: Thiếu thông tin bắt buộc.");
            return;
        }

        // Tạo danh sách đáp án một cách linh hoạt
        List<String> options = new ArrayList<>();
        options.add(option1);
        options.add(option2);
        // Chỉ thêm các đáp án không rỗng
        if (!option3.isEmpty()) options.add(option3);
        if (!option4.isEmpty()) options.add(option4);

        // Tạo đối tượng Question
        Question newQuestion = new Question(questionText, options, 0, topic, level);

        // Gọi ViewModel để thêm câu hỏi và truyền vào Listener để xử lý kết quả
        Log.d(TAG, "Đang gọi questionViewModel.addQuestion...");
        questionViewModel.addQuestion(newQuestion, new QuestionViewModel.OnSaveCompleteListener() {
            @Override
            public void onSaveSuccess() {
                Log.d(TAG, "LƯU THÀNH CÔNG (nhận callback từ ViewModel).");
                Toast.makeText(AddEditQuestionActivity.this, "Thêm câu hỏi thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Đóng Activity khi lưu thành công
            }

            @Override
            public void onSaveFailure(Exception e) {
                Log.e(TAG, "LƯU THẤT BẠI (nhận callback từ ViewModel). Lỗi: ", e);
                Toast.makeText(AddEditQuestionActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Không đóng Activity khi có lỗi để người dùng có thể thử lại
            }
        });
    }
}
