package com.example.studyspace;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoQuizActivity extends AppCompatActivity {

    private TextView tvTitle, tvQuestionCount, tvQuestionContent;
    private RadioGroup radioGroupOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnNext;

    private QuestionViewModel questionViewModel;
    private List<Question> mListQuestions;
    private int currentQuestionIndex = 0;
    private int scoreCount = 0; // Số câu đúng

    // Biến lưu trữ thông tin để gửi lên Firebase
    private String currentTopic;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_quiz);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        // Nhận dữ liệu từ Intent
        currentTopic = getIntent().getStringExtra("TOPIC"); // Lưu vào biến toàn cục
        int level = getIntent().getIntExtra("LEVEL", 1);
        int limit = getIntent().getIntExtra("LIMIT", 10);

        tvTitle.setText("Chủ đề: " + currentTopic);

        // Lấy danh sách câu hỏi từ Firebase
        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);
        questionViewModel.getQuizQuestions(currentTopic, level, limit).observe(this, questions -> {
            if (questions != null && !questions.isEmpty()) {
                mListQuestions = questions;
                showQuestion(0);
            } else {
                Toast.makeText(this, "Không tải được câu hỏi hoặc chưa có câu hỏi nào!", Toast.LENGTH_SHORT).show();
                // Tạm thời không finish() ngay để bạn kịp đọc thông báo lỗi nếu có
                tvQuestionContent.setText("Lỗi: Không tìm thấy câu hỏi.");
            }
        });
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_quiz_title);
        tvQuestionCount = findViewById(R.id.tv_question_count);
        tvQuestionContent = findViewById(R.id.tv_question_content);
        radioGroupOptions = findViewById(R.id.radio_group_options);
        rbOption1 = findViewById(R.id.rb_option_1);
        rbOption2 = findViewById(R.id.rb_option_2);
        rbOption3 = findViewById(R.id.rb_option_3);
        rbOption4 = findViewById(R.id.rb_option_4);
        btnNext = findViewById(R.id.btn_next_question);

        btnNext.setOnClickListener(v -> checkAnswerAndNext());
    }

    private void showQuestion(int index) {
        if (index >= mListQuestions.size()) {
            finishQuiz();
            return;
        }

        Question q = mListQuestions.get(index);
        tvQuestionCount.setText("Câu: " + (index + 1) + "/" + mListQuestions.size());
        tvQuestionContent.setText(q.getQuestionText());

        // Reset RadioButton
        radioGroupOptions.clearCheck();

        List<String> opts = q.getOptions();
        rbOption1.setText(opts.size() > 0 ? opts.get(0) : "");
        rbOption2.setText(opts.size() > 1 ? opts.get(1) : "");
        rbOption3.setText(opts.size() > 2 ? opts.get(2) : "");
        rbOption4.setText(opts.size() > 3 ? opts.get(3) : "");

        // Ẩn bớt nếu ít đáp án
        rbOption3.setVisibility(opts.size() > 2 ? View.VISIBLE : View.GONE);
        rbOption4.setVisibility(opts.size() > 3 ? View.VISIBLE : View.GONE);

        // Đổi tên nút ở câu cuối
        if (index == mListQuestions.size() - 1) {
            btnNext.setText("Nộp bài");
        } else {
            btnNext.setText("Câu tiếp theo");
        }
    }

    private void checkAnswerAndNext() {
        if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn đáp án!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy đáp án người dùng chọn
        int selectedIndex = -1;
        int checkedId = radioGroupOptions.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_option_1) selectedIndex = 0;
        else if (checkedId == R.id.rb_option_2) selectedIndex = 1;
        else if (checkedId == R.id.rb_option_3) selectedIndex = 2;
        else if (checkedId == R.id.rb_option_4) selectedIndex = 3;

        // So sánh với đáp án đúng
        Question currentQ = mListQuestions.get(currentQuestionIndex);
        if (selectedIndex == currentQ.getCorrectAnswerIndex()) {
            scoreCount++;
        }

        // Chuyển câu tiếp
        currentQuestionIndex++;
        showQuestion(currentQuestionIndex);
    }

    private void finishQuiz() {
        int total = mListQuestions.size();

        // Tính điểm trên thang 10
        float finalScore = ((float) scoreCount / total) * 10;
        String scoreFormatted = String.format("%.1f", finalScore);

        // --- LƯU ĐIỂM LÊN FIREBASE ---
        saveScoreToFirebase(finalScore, total, scoreCount);

        // Hiển thị Dialog thông báo kết quả
        new AlertDialog.Builder(this)
                .setTitle("Kết quả bài thi")
                .setMessage("Số câu đúng: " + scoreCount + "/" + total + "\n\n" +
                        "ĐIỂM SỐ: " + scoreFormatted + "/10")
                .setPositiveButton("Hoàn thành", (dialog, which) -> finish()) // Đóng Activity khi bấm OK
                .setCancelable(false)
                .show();
    }

    // Hàm riêng để xử lý lưu vào Firestore
    private void saveScoreToFirebase(float finalScore, int total, int correct) {
        if (mAuth.getCurrentUser() == null) return;

        Map<String, Object> result = new HashMap<>();
        result.put("userId", mAuth.getCurrentUser().getUid());
        result.put("topic", currentTopic);
        // Lưu dạng số để sau này dễ sắp xếp (Query)
        // Dùng parseFloat để làm tròn 1 chữ số thập phân cho đẹp
        result.put("score", Double.parseDouble(String.format("%.1f", finalScore).replace(",", ".")));
        result.put("totalQuestions", total);
        result.put("correctAnswers", correct);
        result.put("timestamp", new Date());

        db.collection("quiz_results")
                .add(result)
                .addOnSuccessListener(documentReference -> {
                    Log.d("DoQuiz", "Lưu điểm thành công!");
                })
                .addOnFailureListener(e -> {
                    Log.e("DoQuiz", "Lỗi lưu điểm: " + e.getMessage());
                    Toast.makeText(DoQuizActivity.this, "Lỗi lưu điểm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}