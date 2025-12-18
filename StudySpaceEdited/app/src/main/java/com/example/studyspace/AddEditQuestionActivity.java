package com.example.studyspace;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup; // MỚI: Import RadioGroup
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

    // MỚI: Khai báo biến RadioGroup để chọn đáp án đúng
    private RadioGroup rgCorrectAnswer;

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

        // MỚI: Ánh xạ RadioGroup (ID phải trùng với file XML vừa sửa)
        rgCorrectAnswer = findViewById(R.id.rg_correct_answer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_edit);

        // Thiết lập Spinner cho độ khó
        setupLevelSpinner();

        // Xử lý nút back trên toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Xử lý nút Lưu
        btnSave.setOnClickListener(v -> saveQuestion());
    }

    private void setupLevelSpinner() {
        // Tạo dữ liệu cho Spinner (Độ khó 1-5)
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

        // 1. Kiểm tra dữ liệu đầu vào cơ bản
        if (TextUtils.isEmpty(questionText) || TextUtils.isEmpty(option1) || TextUtils.isEmpty(option2) || TextUtils.isEmpty(topic)) {
            Toast.makeText(this, "Vui lòng nhập đủ: Câu hỏi, Đáp án A, B và Chủ đề", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Tạo danh sách đáp án
        List<String> options = new ArrayList<>();
        options.add(option1); // Index 0
        options.add(option2); // Index 1
        if (!option3.isEmpty()) options.add(option3); // Index 2 (nếu có)
        if (!option4.isEmpty()) options.add(option4); // Index 3 (nếu có)

        // 3. XÁC ĐỊNH ĐÁP ÁN ĐÚNG (Dựa vào RadioGroup)
        int correctIndex = 0; // Mặc định là A (0)
        int checkedId = rgCorrectAnswer.getCheckedRadioButtonId();

        if (checkedId == R.id.rb_option_b) {
            correctIndex = 1;
        } else if (checkedId == R.id.rb_option_c) {
            correctIndex = 2;
        } else if (checkedId == R.id.rb_option_d) {
            correctIndex = 3;
        }

        // 4. KIỂM TRA LOGIC QUAN TRỌNG:
        // Nếu chọn đáp án đúng là C (index 2) mà danh sách chỉ có 2 phần tử (A, B) -> Lỗi
        if (correctIndex >= options.size()) {
            Toast.makeText(this, "Đáp án đúng bạn chọn (" + (correctIndex == 2 ? "C" : "D") + ") chưa có nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Tạo đối tượng Question (Truyền correctIndex vào thay vì số 0)
        Question newQuestion = new Question(questionText, options, correctIndex, topic, level);

        // 6. Gọi ViewModel để lưu
        Log.d(TAG, "Đang gọi questionViewModel.addQuestion...");
        questionViewModel.addQuestion(newQuestion, new QuestionViewModel.OnSaveCompleteListener() {
            @Override
            public void onSaveSuccess() {
                Log.d(TAG, "LƯU THÀNH CÔNG.");
                Toast.makeText(AddEditQuestionActivity.this, "Thêm câu hỏi thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onSaveFailure(Exception e) {
                Log.e(TAG, "LƯU THẤT BẠI.", e);
                Toast.makeText(AddEditQuestionActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}