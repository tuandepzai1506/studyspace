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
    private String existingQuestionId = null; // null là thêm mới, có giá trị là sửa
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

        String defaultTopic = getIntent().getStringExtra("DEFAULT_TOPIC");
        if (defaultTopic != null) {
            etTopic.setText(defaultTopic);
            etTopic.setEnabled(false); // Khóa ô để giáo viên không sửa nhầm môn
            // Nếu muốn gọn hơn, bạn có thể dùng: etTopic.setVisibility(android.view.View.GONE);
        }
        // MỚI: Ánh xạ RadioGroup (ID phải trùng với file XML vừa sửa)
        rgCorrectAnswer = findViewById(R.id.rg_correct_answer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_edit);
        if (getIntent().hasExtra("EXTRA_QUESTION_ID")) {
            existingQuestionId = getIntent().getStringExtra("EXTRA_QUESTION_ID");
            loadQuestionData(existingQuestionId); // Gọi hàm load
        }
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

        // Kiểm tra Spinner tránh lỗi NullPointer
        if (spinnerLevel.getSelectedItem() == null) return;
        int level = (Integer) spinnerLevel.getSelectedItem();

        // 1. Kiểm tra dữ liệu đầu vào
        if (TextUtils.isEmpty(questionText) || TextUtils.isEmpty(option1) ||
                TextUtils.isEmpty(option2) || TextUtils.isEmpty(topic)) {
            Toast.makeText(this, "Vui lòng nhập đủ: Câu hỏi, Đáp án A, B và Chủ đề", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Tạo danh sách đáp án
        List<String> options = new ArrayList<>();
        options.add(option1);
        options.add(option2);
        if (!option3.isEmpty()) options.add(option3);
        if (!option4.isEmpty()) options.add(option4);

        // 3. Xác định đáp án đúng
        int correctIndex = 0;
        int checkedId = rgCorrectAnswer.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_option_b) correctIndex = 1;
        else if (checkedId == R.id.rb_option_c) correctIndex = 2;
        else if (checkedId == R.id.rb_option_d) correctIndex = 3;

        if (correctIndex >= options.size()) {
            Toast.makeText(this, "Đáp án đúng chưa có nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Khóa nút lưu để tránh nhấn nhiều lần
        btnSave.setEnabled(false);

        // 5. Tạo đối tượng Question
        Question question = new Question(questionText, options, correctIndex, topic, level);

        // 6. LUỒNG LOGIC CHÍNH: Phân biệt Thêm và Sửa
        if (existingQuestionId != null) {
            // CHẾ ĐỘ SỬA
            question.setId(existingQuestionId);
            updateQuestion(question);
        } else {
            // CHẾ ĐỘ THÊM MỚI (Chỉ gọi 1 lần duy nhất ở đây)
            questionViewModel.addQuestion(question, new QuestionViewModel.OnSaveCompleteListener() {
                @Override
                public void onSaveSuccess() {
                    Toast.makeText(AddEditQuestionActivity.this, "Thêm câu hỏi thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onSaveFailure(Exception e) {
                    btnSave.setEnabled(true); // Mở lại nút nếu lỗi
                    Toast.makeText(AddEditQuestionActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void loadQuestionData(String id) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("questions").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Question q = documentSnapshot.toObject(Question.class);
                    if (q != null) {
                        etQuestionText.setText(q.getQuestionText());
                        etTopic.setText(q.getTopic());

                        List<String> opts = q.getOptions();
                        if (opts != null && opts.size() >= 2) {
                            etOption1.setText(opts.get(0));
                            etOption2.setText(opts.get(1));
                            if (opts.size() > 2) etOption3.setText(opts.get(2));
                            if (opts.size() > 3) etOption4.setText(opts.get(3));
                        }

                        // Set đáp án đúng
                        int correctIndex = q.getCorrectAnswerIndex();
                        if (correctIndex == 0) rgCorrectAnswer.check(R.id.rb_option_a);
                        else if (correctIndex == 1) rgCorrectAnswer.check(R.id.rb_option_b);
                        else if (correctIndex == 2) rgCorrectAnswer.check(R.id.rb_option_c);
                        else if (correctIndex == 3) rgCorrectAnswer.check(R.id.rb_option_d);

                        // Set độ khó
                        spinnerLevel.setSelection(q.getLevel() - 1);
                    }
                });
    }
    private void updateQuestion(Question question) {
        // 1. Tham chiếu đến đúng Document ID đang cần sửa
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("questions")
                .document(existingQuestionId) // Dùng ID đã nhận được từ Intent
                .set(question) // .set() sẽ ghi đè toàn bộ dữ liệu mới lên ID cũ này
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật thành công ID: " + existingQuestionId);
                    Toast.makeText(this, "Cập nhật câu hỏi thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Thông báo cho màn hình danh sách biết để load lại dữ liệu
                    finish(); // Đóng màn hình
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật: ", e);
                    Toast.makeText(this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}